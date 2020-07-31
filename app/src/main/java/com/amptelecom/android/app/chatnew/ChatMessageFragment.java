package com.amptelecom.android.app.chatnew;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amptelecom.android.app.LinphoneManager;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.activities.MainActivity;
import com.amptelecom.android.app.chat.ChatMessageViewHolderClickListener;
import com.amptelecom.android.app.chat.ChatMessagesGenericAdapter;
import com.amptelecom.android.app.chatnew.adapter.ChatMessageViewHolder;
import com.amptelecom.android.app.chatnew.adapter.ChatMessagesAdapter;
import com.amptelecom.android.app.chatnew.model.ChatMessage;
import com.amptelecom.android.app.chatnew.model.ChatMessagesResponse;
import com.amptelecom.android.app.contacts.ContactAddress;
import com.amptelecom.android.app.contacts.ContactsManager;
import com.amptelecom.android.app.contacts.LinphoneContact;
import com.amptelecom.android.app.network.ApiService;
import com.amptelecom.android.app.network.RetrofitClientInstance;
import com.amptelecom.android.app.settings.LinphonePreferences;
import com.amptelecom.android.app.utils.FileUtils;
import com.amptelecom.android.app.utils.LinphoneUtils;
import com.amptelecom.android.app.utils.SelectableHelper;
import com.amptelecom.android.app.views.RichEditText;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import okhttp3.ResponseBody;
import org.linphone.core.Address;
import org.linphone.core.ChatRoom;
import org.linphone.core.ChatRoomCapabilities;
import org.linphone.core.Content;
import org.linphone.core.EventLog;
import org.linphone.core.Factory;
import org.linphone.core.Participant;
import org.linphone.core.ParticipantDevice;
import org.linphone.core.tools.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatMessageFragment extends Fragment
        implements ChatMessageViewHolderClickListener,
                SelectableHelper.DeleteListener,
                RichEditText.RichInputListener {
    private static final int ADD_PHOTO = 1337;
    private static final int MESSAGES_PER_PAGE = 20;
    private static final String INPUT_CONTENT_INFO_KEY = "COMMIT_CONTENT_INPUT_CONTENT_INFO";
    private static final String COMMIT_CONTENT_FLAGS_KEY = "COMMIT_CONTENT_FLAGS";

    private ImageView mCallButton;
    private ImageView mBackToCallButton;
    private ImageView mPopupMenu;
    private ImageView mAttachImageButton, mSendMessageButton;
    private TextView mRoomLabel, mParticipantsLabel, mSipUriLabel;
    private RichEditText mMessageTextToSend;
    private LayoutInflater mInflater;
    private RecyclerView mChatEventsList;
    private LinearLayout mFilesUploadLayout;
    private SelectableHelper mSelectionHelper;
    private Context mContext;
    private ViewTreeObserver.OnGlobalLayoutListener mKeyboardListener;
    private Uri mImageToUploadUri;
    private String mRemoteSipUri;
    private Address mLocalSipAddress, mRemoteSipAddress, mRemoteParticipantAddress;
    private ChatRoom mChatRoom;
    private ArrayList<LinphoneContact> mParticipants;
    private int mContextMenuMessagePosition;
    private LinearLayout mTopBar;

    private InputContentInfoCompat mCurrentInputContentInfo;
    private String chatid = "", to = "", cc = "";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain the fragment across configuration changes
        setRetainInstance(true);

        if (getArguments() != null) {
            if (getArguments().getString("LocalSipUri") != null) {
                String mLocalSipUri = getArguments().getString("LocalSipUri");
                mLocalSipAddress = Factory.instance().createAddress(mLocalSipUri);
            }
            if (getArguments().getString("chatid") != null) {
                chatid = getArguments().getString("chatid");
            }
            if (getArguments().getString("to") != null) {
                to = getArguments().getString("to");
            }
            if (getArguments().getString("cc") != null) {
                cc = getArguments().getString("cc");
            }
            if (getArguments().getString("RemoteSipUri") != null) {
                mRemoteSipUri = getArguments().getString("RemoteSipUri");
                mRemoteSipAddress = Factory.instance().createAddress(mRemoteSipUri);
            }
        }

        mContext = getActivity().getApplicationContext();
        mInflater = inflater;
        View view = inflater.inflate(R.layout.chat_new, container, false);

        mTopBar = view.findViewById(R.id.top_bar);

        ImageView backButton = view.findViewById(R.id.back);
        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ChatActivity) getActivity()).goBack();
                    }
                });
        backButton.setVisibility(
                getResources().getBoolean(R.bool.isTablet) ? View.INVISIBLE : View.VISIBLE);

        mCallButton = view.findViewById(R.id.start_call);
        mCallButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinphoneManager.getCallManager()
                                .newOutgoingCall(mRemoteParticipantAddress.asString(), null);
                    }
                });

        mBackToCallButton = view.findViewById(R.id.back_to_call);
        mBackToCallButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).goBackToCall();
                    }
                });

        mPopupMenu = view.findViewById(R.id.menu);
        mPopupMenu.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu();
                    }
                });

        mRoomLabel = view.findViewById(R.id.subject);
        mRoomLabel.setText(to);
        mParticipantsLabel = view.findViewById(R.id.participants);
        mSipUriLabel = view.findViewById(R.id.sipUri);

        mFilesUploadLayout = view.findViewById(R.id.file_upload_layout);

        mAttachImageButton = view.findViewById(R.id.send_picture);
        mAttachImageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] permissions = {
                            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
                        };
                        ((ChatActivity) getActivity()).requestPermissionsIfNotGranted(permissions);
                        pickFile();
                    }
                });
        if (getResources().getBoolean(R.bool.disable_chat_send_file)) {
            mAttachImageButton.setEnabled(false);
            mAttachImageButton.setVisibility(View.GONE);
        }

        mSendMessageButton = view.findViewById(R.id.send_message);
        mSendMessageButton.setEnabled(false);
        mSendMessageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage();
                    }
                });

        mMessageTextToSend = view.findViewById(R.id.message);
        mMessageTextToSend.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mSendMessageButton.setEnabled(
                                mMessageTextToSend.getText().length() > 0
                                        || mFilesUploadLayout.getChildCount() > 0);
                        if (mChatRoom != null && mMessageTextToSend.getText().length() > 0) {
                            if (!getResources().getBoolean(R.bool.allow_multiple_images_and_text)) {
                                mAttachImageButton.setEnabled(false);
                            }
                            mChatRoom.compose();
                        } else {
                            mAttachImageButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
        mMessageTextToSend.clearFocus();
        mMessageTextToSend.setListener(this);

        mChatEventsList = view.findViewById(R.id.chat_message_list);
        mSelectionHelper = new SelectableHelper(view, this);
        LinearLayoutManager layoutManager =
                new ChatMessageFragment.LinphoneLinearLayoutManager(
                        mContext, LinearLayoutManager.VERTICAL, true);
        mChatEventsList.setLayoutManager(layoutManager);

        ChatScrollListener chatScrollListener =
                new ChatScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int totalItemsCount) {
                        //                        loadMoreData(totalItemsCount);
                    }
                };
        mChatEventsList.addOnScrollListener(chatScrollListener);

        if (getArguments() != null) {
            String fileSharedUri = getArguments().getString("SharedFiles");
            if (fileSharedUri != null) {
                Log.i("[Chat Messages Fragment] Found shared file(s): " + fileSharedUri);
                if (fileSharedUri.contains(":")) {
                    String[] files = fileSharedUri.split(":");
                    for (String file : files) {
                        addFileIntoSharingArea(file);
                    }
                } else {
                    addFileIntoSharingArea(fileSharedUri);
                }
            }

            if (getArguments().containsKey("SharedText")) {
                String sharedText = getArguments().getString("SharedText");
                mMessageTextToSend.setText(sharedText);
                Log.i("[Chat Messages Fragment] Found shared text: " + sharedText);
            }
        }

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        fetchHistory();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        addVirtualKeyboardVisiblityListener();
        // Force hide keyboard
        getActivity()
                .getWindow()
                .setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        if (getActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        //        fetchHistory();
        //        displayChatRoomHistory();

        //        LinphoneContext.instance()
        //                .getNotificationManager()
        //                .setCurrentlyDisplayedChatRoom(
        //                        mRemoteSipAddress != null ? mRemoteSipAddress.asStringUriOnly() :
        // null);
    }

    ChatMessagesAdapter mEventsAdapter;
    ProgressDialog progressDialog;

    private void fetchHistory() {
        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ChatMessagesResponse> call =
                service.getMessages(
                        LinphonePreferences.instance().getUsername(),
                        LinphonePreferences.instance().getDomain(),
                        LinphonePreferences.instance().getPassword(),
                        UUID.randomUUID().toString(),
                        "",
                        "",
                        chatid);
        android.util.Log.i("ttt", "called" + chatid);

        call.enqueue(
                new Callback<ChatMessagesResponse>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagesResponse> call,
                            Response<ChatMessagesResponse> response) {
                        progressDialog.dismiss();
                        try {
                            ArrayList<ChatMessage> list = response.body().getData();
                            Collections.reverse(list);
                            mEventsAdapter =
                                    new ChatMessagesAdapter(
                                            ChatMessageFragment.this,
                                            mSelectionHelper,
                                            R.layout.chat_bubble_new,
                                            list,
                                            ChatMessageFragment.this);
                            mSelectionHelper.setAdapter(mEventsAdapter);
                            mChatEventsList.setAdapter(mEventsAdapter);
                            scrollToBottom();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        android.util.Log.i("ttt", "success");
                    }

                    @Override
                    public void onFailure(Call<ChatMessagesResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        android.util.Log.i("ttt", "failure2" + t.getLocalizedMessage());
                    }
                });
    }

    private void fetchHistory2() {
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call =
                service.getMessages2(
                        LinphonePreferences.instance().getUsername(),
                        LinphonePreferences.instance().getDomain(),
                        LinphonePreferences.instance().getPassword(),
                        UUID.randomUUID().toString(),
                        "",
                        "",
                        chatid);
        android.util.Log.i("ttt", "called" + chatid);

        call.enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            android.util.Log.i("ttt", "success" + response.body().string());
                            //                            ArrayList<ChatMessage> list =
                            // response.body().getData();
                            //                            ChatMessagesAdapter mEventsAdapter =
                            //                                    new ChatMessagesAdapter(
                            //                                            ChatMessageFragment.this,
                            //                                            mSelectionHelper,
                            //                                            R.layout.chat_bubble_new,
                            //                                            list,
                            //                                            ChatMessageFragment.this);
                            //
                            // mSelectionHelper.setAdapter(mEventsAdapter);
                            //
                            // mChatEventsList.setAdapter(mEventsAdapter);
                            //                            scrollToBottom();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        android.util.Log.i("ttt", "success");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        android.util.Log.i("ttt", "failure");
                    }
                });
    }

    @Override
    public void onPause() {
        removeVirtualKeyboardVisiblityListener();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> files = new ArrayList<>();
        for (int i = 0; i < mFilesUploadLayout.getChildCount(); i++) {
            View child = mFilesUploadLayout.getChildAt(i);
            String filePath = (String) child.getTag();
            files.add(filePath);
        }
        outState.putStringArrayList("Files", files);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == ADD_PHOTO && resultCode == Activity.RESULT_OK) {
                String fileToUploadPath = null;
                if (data.getData() != null) {
                    Log.i(
                            "[Chat Messages Fragment] Intent data after picking file is "
                                    + data.getData().toString());
                    if (data.getData().toString().contains("com.android.contacts/contacts/")) {
                        Uri cvsPath = FileUtils.getCVSPathFromLookupUri(data.getData().toString());
                        if (cvsPath != null) {
                            fileToUploadPath = cvsPath.toString();
                            Log.i("[Chat Messages Fragment] Found CVS path: " + fileToUploadPath);
                        } else {
                            // TODO Error
                            return;
                        }
                    } else {
                        fileToUploadPath =
                                FileUtils.getRealPathFromURI(getActivity(), data.getData());
                        Log.i(
                                "[Chat Messages Fragment] Resolved path for data is: "
                                        + fileToUploadPath);
                    }
                    if (fileToUploadPath == null) {
                        fileToUploadPath = data.getData().toString();
                        Log.i(
                                "[Chat Messages Fragment] Couldn't resolve path, using as-is: "
                                        + fileToUploadPath);
                    }
                } else if (mImageToUploadUri != null) {
                    fileToUploadPath = mImageToUploadUri.getPath();
                    Log.i(
                            "[Chat Messages Fragment] Using pre-created path for dynamic capture "
                                    + fileToUploadPath);
                }

                if (fileToUploadPath.startsWith("content://")
                        || fileToUploadPath.startsWith("file://")) {
                    Uri uriToParse = Uri.parse(fileToUploadPath);
                    fileToUploadPath =
                            FileUtils.getFilePath(
                                    getActivity().getApplicationContext(), uriToParse);
                    Log.i(
                            "[Chat Messages Fragment] Path was using a content or file scheme, real path is: "
                                    + fileToUploadPath);
                    if (fileToUploadPath == null) {
                        Log.e(
                                "[Chat Messages Fragment] Failed to get access to file "
                                        + uriToParse.toString());
                    }
                } else if (fileToUploadPath.contains("com.android.contacts/contacts/")) {
                    fileToUploadPath =
                            FileUtils.getCVSPathFromLookupUri(fileToUploadPath).toString();
                    Log.i(
                            "[Chat Messages Fragment] Path was using a contact scheme, real path is: "
                                    + fileToUploadPath);
                }

                if (fileToUploadPath != null) {
                    if (FileUtils.isExtensionImage(fileToUploadPath)) {
                        addImageToPendingList(fileToUploadPath);
                    } else {
                        addFileToPendingList(fileToUploadPath);
                    }
                } else {
                    Log.e(
                            "[Chat Messages Fragment] Failed to get a path that we could use, aborting attachment");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            if (FileUtils.isExtensionImage(mImageToUploadUri.getPath())) {
                File file = new File(mImageToUploadUri.getPath());
                if (file.exists()) {
                    addImageToPendingList(mImageToUploadUri.getPath());
                }
            }
        }
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        for (Object obj : objectsToDelete) {
            EventLog eventLog = (EventLog) obj;
            LinphoneUtils.deleteFileContentIfExists(eventLog);
            eventLog.deleteFromDatabase();
        }
        ((ChatMessagesGenericAdapter) mChatEventsList.getAdapter())
                .refresh(mChatRoom.getHistoryEvents(MESSAGES_PER_PAGE));
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ChatMessageViewHolder holder = (ChatMessageViewHolder) v.getTag();
        mContextMenuMessagePosition = holder.getAdapterPosition();

        EventLog event =
                (EventLog)
                        ((ChatMessagesGenericAdapter) mChatEventsList.getAdapter())
                                .getItem(mContextMenuMessagePosition);
        if (event.getType() != EventLog.Type.ConferenceChatMessage) {
            return;
        }

        MenuInflater inflater = getActivity().getMenuInflater();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        return super.onContextItemSelected(item);
    }

    private void addFileIntoSharingArea(String fileSharedUri) {
        if (FileUtils.isExtensionImage(fileSharedUri)) {
            addImageToPendingList(fileSharedUri);
        } else {
            if (fileSharedUri.startsWith("content://") || fileSharedUri.startsWith("file://")) {
                fileSharedUri =
                        FileUtils.getFilePath(
                                getActivity().getApplicationContext(), Uri.parse(fileSharedUri));
            } else if (fileSharedUri.contains("com.android.contacts/contacts/")) {
                fileSharedUri = FileUtils.getCVSPathFromLookupUri(fileSharedUri).toString();
            }
            addFileToPendingList(fileSharedUri);
        }
    }

    private void loadMoreData(final int totalItemsCount) {
        LinphoneUtils.dispatchOnUIThread(
                new Runnable() {
                    @Override
                    public void run() {
                        int maxSize = mChatRoom.getHistoryEventsSize();
                        if (totalItemsCount < maxSize) {
                            int upperBound = totalItemsCount + MESSAGES_PER_PAGE;
                            if (upperBound > maxSize) {
                                upperBound = maxSize;
                            }
                            EventLog[] newLogs;
                            newLogs = mChatRoom.getHistoryRangeEvents(totalItemsCount, upperBound);
                            ArrayList<EventLog> logsList = new ArrayList<>(Arrays.asList(newLogs));
                            ((ChatMessagesGenericAdapter) mChatEventsList.getAdapter())
                                    .addAllToHistory(logsList);
                        }
                    }
                });
    }

    /** Keyboard management */
    private void addVirtualKeyboardVisiblityListener() {
        mKeyboardListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect visibleArea = new Rect();
                        getActivity()
                                .getWindow()
                                .getDecorView()
                                .getWindowVisibleDisplayFrame(visibleArea);

                        int screenHeight =
                                getActivity().getWindow().getDecorView().getRootView().getHeight();
                        int heightDiff = screenHeight - (visibleArea.bottom - visibleArea.top);
                        if (heightDiff > screenHeight * 0.15) {
                            showKeyboardVisibleMode();
                        } else {
                            hideKeyboardVisibleMode();
                        }
                    }
                };
        getActivity()
                .getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(mKeyboardListener);
    }

    private void removeVirtualKeyboardVisiblityListener() {
        getActivity()
                .getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .removeOnGlobalLayoutListener(mKeyboardListener);
        hideKeyboardVisibleMode();
    }

    private void showKeyboardVisibleMode() {
        ((ChatActivity) getActivity()).hideTabBar();
        ((ChatActivity) getActivity()).hideStatusBar();
        mTopBar.setVisibility(View.GONE);
    }

    private void hideKeyboardVisibleMode() {
        if (!getResources().getBoolean(R.bool.hide_bottom_bar_on_second_level_views)) {
            ((ChatActivity) getActivity()).showTabBar();
        }
        ((ChatActivity) getActivity()).showStatusBar();
        mTopBar.setVisibility(View.VISIBLE);
    }

    /** View initialization */
    private void setReadOnly() {
        mMessageTextToSend.setEnabled(false);
        mAttachImageButton.setEnabled(false);
        mSendMessageButton.setEnabled(false);
    }

    private void getContactsForParticipants() {
        mParticipants = new ArrayList<>();
        if (mChatRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
            LinphoneContact c =
                    ContactsManager.getInstance().findContactFromAddress(mRemoteParticipantAddress);
            if (c != null) {
                mParticipants.add(c);
            }
        } else {
            int index = 0;
            StringBuilder participantsLabel = new StringBuilder();
            for (Participant p : mChatRoom.getParticipants()) {
                LinphoneContact c =
                        ContactsManager.getInstance().findContactFromAddress(p.getAddress());
                if (c != null) {
                    mParticipants.add(c);
                    participantsLabel.append(c.getFullName());
                } else {
                    String displayName = LinphoneUtils.getAddressDisplayName(p.getAddress());
                    participantsLabel.append(displayName);
                }
                index++;
                if (index != mChatRoom.getNbParticipants()) participantsLabel.append(", ");
            }
            mParticipantsLabel.setText(participantsLabel.toString());
        }

        if (mChatEventsList.getAdapter() != null) {
            ((ChatMessagesGenericAdapter) mChatEventsList.getAdapter()).setContacts(mParticipants);
        }
    }

    private void displayChatRoomHistory() {
        if (mChatRoom == null) return;
        //        ChatMessagesAdapter mEventsAdapter =
        //                new ChatMessagesAdapter(
        //                        this,
        //                        mSelectionHelper,
        //                        R.layout.chat_bubble_new,
        //                        mChatRoom.getHistoryEvents(MESSAGES_PER_PAGE),
        //                        mParticipants,
        //                        this);
        //        mSelectionHelper.setAdapter(mEventsAdapter);
        //        mChatEventsList.setAdapter(mEventsAdapter);
        //        scrollToBottom();
    }

    private void showSecurityDialog(boolean oneParticipantOneDevice) {
        final Dialog dialog =
                ((ChatActivity) getActivity())
                        .displayDialog(getString(R.string.lime_security_popup));
        Button delete = dialog.findViewById(R.id.dialog_delete_button);
        delete.setVisibility(View.GONE);
        Button ok = dialog.findViewById(R.id.dialog_ok_button);
        ok.setText(oneParticipantOneDevice ? getString(R.string.call) : getString(R.string.ok));
        ok.setVisibility(View.VISIBLE);
        Button cancel = dialog.findViewById(R.id.dialog_cancel_button);
        cancel.setText(getString(R.string.cancel));

        dialog.findViewById(R.id.dialog_do_not_ask_again_layout).setVisibility(View.VISIBLE);
        final CheckBox doNotAskAgain = dialog.findViewById(R.id.doNotAskAgain);
        dialog.findViewById(R.id.doNotAskAgainLabel)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doNotAskAgain.setChecked(!doNotAskAgain.isChecked());
                            }
                        });

        ok.setTag(oneParticipantOneDevice);
        ok.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean oneParticipantOneDevice = (boolean) view.getTag();
                        if (doNotAskAgain.isChecked()) {
                            LinphonePreferences.instance().enableLimeSecurityPopup(false);
                        }

                        if (oneParticipantOneDevice) {
                            ParticipantDevice device =
                                    mChatRoom.getParticipants()[0].getDevices()[0];
                            LinphoneManager.getCallManager()
                                    .inviteAddress(device.getAddress(), true);
                        } else {
                            ((ChatActivity) getActivity())
                                    .showDevices(mLocalSipAddress, mRemoteSipAddress);
                        }

                        dialog.dismiss();
                    }
                });

        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (doNotAskAgain.isChecked()) {
                            LinphonePreferences.instance().enableLimeSecurityPopup(false);
                        }
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void scrollToBottom() {
        mChatEventsList.getLayoutManager().scrollToPosition(0);
    }

    @Override
    public void onItemClicked(int position) {
        if (mSelectionHelper.getAdapter().isEditionEnabled()) {
            mSelectionHelper.getAdapter().toggleSelection(position);
        }
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        ArrayList<String> files = savedInstanceState.getStringArrayList("Files");
        if (files != null && !files.isEmpty()) {
            for (String file : files) {
                if (FileUtils.isExtensionImage(file)) {
                    addImageToPendingList(file);
                } else {
                    addFileToPendingList(file);
                }
            }
        }

        final InputContentInfoCompat previousInputContentInfo =
                InputContentInfoCompat.wrap(
                        savedInstanceState.getParcelable(INPUT_CONTENT_INFO_KEY));
        final int previousFlags = savedInstanceState.getInt(COMMIT_CONTENT_FLAGS_KEY);
        if (previousInputContentInfo != null) {
            onCommitContentInternal(previousInputContentInfo, previousFlags);
        }
    }

    private void pickFile() {
        List<Intent> cameraIntents = new ArrayList<>();

        // Handles image & video picking
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("*/*");
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});

        // Allows to capture directly from the camera
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file =
                new File(
                        FileUtils.getStorageDirectory(mContext),
                        getString(R.string.temp_photo_name_with_date)
                                .replace("%s", System.currentTimeMillis() + ".jpeg"));
        mImageToUploadUri = Uri.fromFile(file);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageToUploadUri);
        cameraIntents.add(captureIntent);

        // Finally allow any kind of file
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        cameraIntents.add(fileIntent);

        Intent chooserIntent =
                Intent.createChooser(galleryIntent, getString(R.string.image_picker_title));
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));

        startActivityForResult(chooserIntent, ADD_PHOTO);
    }

    private void addFileToPendingList(String path) {
        if (path == null) {
            Log.e(
                    "[Chat Messages Fragment] Can't add file to pending list because it's path is null...");
            return;
        }

        View pendingFile = mInflater.inflate(R.layout.file_upload_cell, mFilesUploadLayout, false);
        pendingFile.setTag(path);

        TextView text = pendingFile.findViewById(R.id.pendingFileForUpload);
        String extension = path.substring(path.lastIndexOf('.'));
        text.setText(extension);

        ImageView remove = pendingFile.findViewById(R.id.remove);
        remove.setTag(pendingFile);
        remove.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View pendingImage = (View) view.getTag();
                        mFilesUploadLayout.removeView(pendingImage);
                        mAttachImageButton.setEnabled(true);
                        mMessageTextToSend.setEnabled(true);
                        mSendMessageButton.setEnabled(
                                mMessageTextToSend.getText().length() > 0
                                        || mFilesUploadLayout.getChildCount() > 0);
                    }
                });

        mFilesUploadLayout.addView(pendingFile);

        if (!getResources().getBoolean(R.bool.allow_multiple_images_and_text)) {
            mAttachImageButton.setEnabled(false);
            mMessageTextToSend.setEnabled(false);
        }
        mSendMessageButton.setEnabled(true);
    }

    private void addImageToPendingList(String path) {
        if (path == null) {
            Log.e(
                    "[Chat Messages Fragment] Can't add image to pending list because it's path is null...");
            return;
        }

        View pendingImage =
                mInflater.inflate(R.layout.image_upload_cell, mFilesUploadLayout, false);
        pendingImage.setTag(path);

        ImageView image = pendingImage.findViewById(R.id.pendingImageForUpload);
        Glide.with(mContext).load(path).into(image);

        ImageView remove = pendingImage.findViewById(R.id.remove);
        remove.setTag(pendingImage);
        remove.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View pendingImage = (View) view.getTag();
                        mFilesUploadLayout.removeView(pendingImage);
                        mAttachImageButton.setEnabled(true);
                        mMessageTextToSend.setEnabled(true);
                        mSendMessageButton.setEnabled(
                                mMessageTextToSend.getText().length() > 0
                                        || mFilesUploadLayout.getChildCount() > 0);
                    }
                });

        mFilesUploadLayout.addView(pendingImage);

        if (!getResources().getBoolean(R.bool.allow_multiple_images_and_text)) {
            mAttachImageButton.setEnabled(false);
            mMessageTextToSend.setEnabled(false);
        }
        mSendMessageButton.setEnabled(true);
    }

    private void sendMessage() {
        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ChatMessagesResponse> call =
                service.sendMessage(
                        LinphonePreferences.instance().getUsername(),
                        LinphonePreferences.instance().getDomain(),
                        LinphonePreferences.instance().getPassword(),
                        UUID.randomUUID().toString(),
                        to,
                        cc,
                        mMessageTextToSend.getText().toString(),
                        "[]", chatid);
        android.util.Log.i("ttt", "called" + cc);
        call.enqueue(
                new Callback<ChatMessagesResponse>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagesResponse> call,
                            Response<ChatMessagesResponse> response) {
                        progressDialog.dismiss();
                        mMessageTextToSend.setText("");
                        try {
                            mEventsAdapter.addItem(response.body().getData().get(0));
                            scrollToBottom();
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessagesResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        android.util.Log.i("ttt", "failure");
                    }
                });
    }

    /** Message sending */
    private void sendMessageOld() {
        org.linphone.core.ChatMessage msg = mChatRoom.createEmptyMessage();
        boolean isBasicChatRoom = mChatRoom.hasCapability(ChatRoomCapabilities.Basic.toInt());
        boolean sendMultipleImagesAsDifferentMessages =
                getResources().getBoolean(R.bool.send_multiple_images_as_different_messages);
        boolean sendImageAndTextAsDifferentMessages =
                getResources().getBoolean(R.bool.send_text_and_images_as_different_messages);

        String text = mMessageTextToSend.getText().toString();
        boolean hasText = text != null && text.length() > 0;

        int filesCount = mFilesUploadLayout.getChildCount();
        for (int i = 0; i < filesCount; i++) {
            String filePath = (String) mFilesUploadLayout.getChildAt(i).getTag();
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            String extension = FileUtils.getExtensionFromFileName(fileName);
            Content content = Factory.instance().createContent();
            if (FileUtils.isExtensionImage(fileName)) {
                content.setType("image");
            } else {
                content.setType("file");
            }
            content.setSubtype(extension);
            content.setName(fileName);
            content.setFilePath(filePath); // Let the file body handler take care of the upload

            boolean split =
                    isBasicChatRoom; // Always split contents in basic chat rooms for compatibility
            if (hasText && sendImageAndTextAsDifferentMessages) {
                split = true;
            } else if (mFilesUploadLayout.getChildCount() > 1
                    && sendMultipleImagesAsDifferentMessages) {
                split = true;

                // Allow the last image to be sent with text if image and text at the same time OK
                if (hasText && i == filesCount - 1) {
                    split = false;
                }
            }

            if (split) {
                org.linphone.core.ChatMessage fileMessage =
                        mChatRoom.createFileTransferMessage(content);
                fileMessage.send();
            } else {
                msg.addFileContent(content);
            }
        }

        if (hasText) {
            msg.addTextContent(text);
        }

        // Set listener not required here anymore, message will be added to messages list and
        // adapter will set the listener
        if (msg.getContents().length > 0) {
            msg.send();
        }

        mFilesUploadLayout.removeAllViews();
        mAttachImageButton.setEnabled(true);
        mMessageTextToSend.setEnabled(true);
        mMessageTextToSend.setText("");
    }

    private void showPopupMenu() {
        MenuBuilder builder = new MenuBuilder(getActivity());
        MenuPopupHelper popupMenu = new MenuPopupHelper(getActivity(), builder, mPopupMenu);
        popupMenu.setForceShowIcon(true);

        new MenuInflater(getActivity()).inflate(R.menu.chat_room_menu, builder);

        if (mChatRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
            builder.removeItem(R.id.chat_room_group_info);
        }

        if (!mChatRoom.hasCapability(ChatRoomCapabilities.Encrypted.toInt())) {
            builder.removeItem(R.id.chat_room_participants_devices);
            builder.removeItem(R.id.chat_room_ephemeral_messages);
        } else {
            if (!LinphonePreferences.instance().isEphemeralMessagesEnabled()) {
                builder.removeItem(R.id.chat_room_ephemeral_messages);
            }
        }

        builder.setCallback(
                new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        if (item.getItemId() == R.id.chat_room_group_info) {
                            goToGroupInfo();
                            return true;
                        } else if (item.getItemId() == R.id.chat_room_participants_devices) {
                            goToDevices();
                            return true;
                        } else if (item.getItemId() == R.id.chat_room_ephemeral_messages) {
                            goToEphemeral();
                            return true;
                        } else if (item.getItemId() == R.id.chat_room_delete_messages) {
                            mSelectionHelper.enterEditionMode();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });

        popupMenu.show();
    }

    private void goToDevices() {
        boolean oneParticipantOneDevice = false;
        if (mChatRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
            ParticipantDevice[] devices = mChatRoom.getParticipants()[0].getDevices();
            // Only start a call automatically if both ourselves and the remote
            // have 1 device exactly, otherwise show devices list.
            oneParticipantOneDevice =
                    devices.length == 1 && mChatRoom.getMe().getDevices().length == 1;
        }

        if (LinphonePreferences.instance().isLimeSecurityPopupEnabled()) {
            showSecurityDialog(oneParticipantOneDevice);
        } else {
            if (oneParticipantOneDevice) {
                ParticipantDevice device = mChatRoom.getParticipants()[0].getDevices()[0];
                LinphoneManager.getCallManager().inviteAddress(device.getAddress(), true);
            } else {
                ((ChatActivity) getActivity()).showDevices(mLocalSipAddress, mRemoteSipAddress);
            }
        }
    }

    private void goToGroupInfo() {
        if (mChatRoom == null) return;
        ArrayList<ContactAddress> participants = new ArrayList<>();
        for (Participant p : mChatRoom.getParticipants()) {
            Address a = p.getAddress();
            LinphoneContact c = ContactsManager.getInstance().findContactFromAddress(a);
            if (c == null) {
                c = new LinphoneContact();
                String displayName = LinphoneUtils.getAddressDisplayName(a);
                c.setFullName(displayName);
            }
            ContactAddress ca = new ContactAddress(c, a.asString(), "", p.isAdmin());
            participants.add(ca);
        }

        boolean encrypted = mChatRoom.hasCapability(ChatRoomCapabilities.Encrypted.toInt());
        ((ChatActivity) getActivity())
                .showChatRoomGroupInfo(
                        mRemoteSipAddress, participants, mChatRoom.getSubject(), encrypted);
    }

    private void goToEphemeral() {
        if (mChatRoom == null) return;
        ((ChatActivity) getActivity()).showChatRoomEphemeral(mRemoteSipAddress);
    }

    /*
     * Chat room callbacks
     */

    @Override
    public boolean onCommitContent(
            InputContentInfoCompat inputContentInfo,
            int flags,
            Bundle opts,
            String[] contentMimeTypes) {
        try {
            if (mCurrentInputContentInfo != null) {
                mCurrentInputContentInfo.releasePermission();
            }
        } catch (Exception e) {
            Log.e("[Chat Messages Fragment] releasePermission failed : ", e);
        } finally {
            mCurrentInputContentInfo = null;
        }

        boolean supported = false;
        for (final String mimeType : contentMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            return false;
        }

        return onCommitContentInternal(inputContentInfo, flags);
    }

    private boolean onCommitContentInternal(InputContentInfoCompat inputContentInfo, int flags) {
        if ((flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                Log.e("[Chat Messages Fragment] requestPermission failed : ", e);
                return false;
            }
        }

        if (inputContentInfo.getContentUri() != null) {
            String contentUri = FileUtils.getFilePath(mContext, inputContentInfo.getContentUri());
            addImageToPendingList(contentUri);
        }

        mCurrentInputContentInfo = inputContentInfo;

        return true;
    }

    // This is a workaround to prevent a crash from happening while rotating the device
    private class LinphoneLinearLayoutManager extends LinearLayoutManager {
        LinphoneLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e(
                        "[Chat Messages Fragment] InvalidIndexOutOfBound Exception, probably while rotating the device");
            }
        }
    }
}
