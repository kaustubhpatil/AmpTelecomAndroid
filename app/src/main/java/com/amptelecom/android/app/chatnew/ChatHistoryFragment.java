package com.amptelecom.android.app.chatnew;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amptelecom.android.app.LinphoneManager;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.activities.MainActivity;
import com.amptelecom.android.app.call.views.LinphoneLinearLayoutManager;
import com.amptelecom.android.app.chatnew.adapter.ChatHistoryAdapter;
import com.amptelecom.android.app.chatnew.adapter.ChatHistoryViewHolder;
import com.amptelecom.android.app.chatnew.model.ChatConversationsResponse;
import com.amptelecom.android.app.chatnew.model.ChatData;
import com.amptelecom.android.app.contacts.ContactsUpdatedListener;
import com.amptelecom.android.app.network.ApiService;
import com.amptelecom.android.app.network.RetrofitClientInstance;
import com.amptelecom.android.app.settings.LinphonePreferences;
import com.amptelecom.android.app.utils.LinphoneUtils;
import com.amptelecom.android.app.utils.SelectableHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import okhttp3.ResponseBody;
import org.linphone.core.ChatRoom;
import org.linphone.core.ChatRoomListenerStub;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.EventLog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatHistoryFragment extends Fragment
        implements ContactsUpdatedListener,
                ChatHistoryViewHolder.ClickListener,
                SelectableHelper.DeleteListener {

    private RecyclerView mChatRoomsList;
    private ImageView mNewGroupDiscussionButton;
    private ImageView mBackToCallButton;
    private ChatHistoryAdapter mChatRoomsAdapter;
    private CoreListenerStub mListener;
    private RelativeLayout mWaitLayout;
    private int mChatRoomDeletionPendingCount;
    private ChatRoomListenerStub mChatRoomListener;
    private SelectableHelper mSelectionHelper;
    private TextView mNoChatHistory;
    private View view;
    ProgressBar progress_bar;

    @Override
    public View onCreateView(
            final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.chatlist_new, container, false);

        mChatRoomsList = view.findViewById(R.id.chatList);
        progress_bar = view.findViewById(R.id.progress_bar);
        mWaitLayout = view.findViewById(R.id.waitScreen);
        ImageView newDiscussionButton = view.findViewById(R.id.new_discussion);
        mNewGroupDiscussionButton = view.findViewById(R.id.new_group_discussion);
        mBackToCallButton = view.findViewById(R.id.back_in_call);
        mNoChatHistory = view.findViewById(R.id.noChatHistory);

        mWaitLayout.setVisibility(View.GONE);

        newDiscussionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChatActivity) getActivity())
                                .showChatRoomCreation(null, null, null, false, false, false);
                    }
                });

        mNewGroupDiscussionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChatActivity) getActivity())
                                .showChatRoomCreation(null, null, null, false, true, false);
                    }
                });

        mBackToCallButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).goBackToCall();
                    }
                });

        getConversations();
        return view;
    }

    private void showChatList(ArrayList<ChatData> list) {
        ChatRoom[] rooms = LinphoneManager.getCore().getChatRooms();
        List<ChatRoom> mRooms = Arrays.asList(rooms);

        mSelectionHelper = new SelectableHelper(view, this);
        mChatRoomsAdapter =
                new ChatHistoryAdapter(
                        getActivity(), R.layout.chatlist_cell_new, list, this, mSelectionHelper);

        mChatRoomsList.setAdapter(mChatRoomsAdapter);
        mSelectionHelper.setAdapter(mChatRoomsAdapter);
        mSelectionHelper.setDialogMessage(R.string.chat_room_delete_dialog);

        LinearLayoutManager layoutManager = new LinphoneLinearLayoutManager(getActivity());
        mChatRoomsList.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(
                        mChatRoomsList.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(
                getActivity()
                        .getApplicationContext()
                        .getResources()
                        .getDrawable(R.drawable.divider));
        mChatRoomsList.addItemDecoration(dividerItemDecoration);
        //        mChatRoomListener =
        //                new ChatRoomListenerStub() {
        //                    @Override
        //                    public void onStateChanged(ChatRoom room, ChatRoom.State state) {
        //                        super.onStateChanged(room, state);
        //                        if (state == ChatRoom.State.Deleted
        //                                || state == ChatRoom.State.TerminationFailed) {
        //                            mChatRoomDeletionPendingCount -= 1;
        //
        //                            if (state == ChatRoom.State.TerminationFailed) {
        //                                // TODO error message
        //                            }
        //
        //                            if (mChatRoomDeletionPendingCount == 0) {
        //                                mWaitLayout.setVisibility(View.GONE);
        //                                refreshChatRoomsList();
        //                            }
        //                        }
        //                    }
        //                };
    }

    private void getConversations2() {
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call =
                service.getConversations2(
                        LinphonePreferences.instance().getUsername(),
                        LinphonePreferences.instance().getDomain(),
                        LinphonePreferences.instance().getPassword(),
                        UUID.randomUUID().toString());
        call.enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            progress_bar.setVisibility(View.GONE);
                            android.util.Log.i("ttt", "failure" + response.body().string());
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        android.util.Log.i("ttt", "failure");
                    }
                });
    }

    private void getConversations() {
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ChatConversationsResponse> call =
                service.getConversations(
                        LinphonePreferences.instance().getUsername(),
                        LinphonePreferences.instance().getDomain(),
                        LinphonePreferences.instance().getPassword(),
                        UUID.randomUUID().toString());
        call.enqueue(
                new Callback<ChatConversationsResponse>() {
                    @Override
                    public void onResponse(
                            Call<ChatConversationsResponse> call,
                            Response<ChatConversationsResponse> response) {
                        try {
                            progress_bar.setVisibility(View.GONE);
                            showChatList(response.body().getData());
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatConversationsResponse> call, Throwable t) {
                        android.util.Log.i("ttt", "failure" + t.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void onItemClicked(int position) {
        //        if (mChatRoomsAdapter.isEditionEnabled()) {
        //            mChatRoomsAdapter.toggleSelection(position);
        //        } else {
        ChatData room = (ChatData) mChatRoomsAdapter.getItem(position);
        if (room != null) {
            String to = "";
            if (room.getDirection().equals("inbound")) {
                to = room.getFrom();
            } else {
                to = room.getTo();
            }
            ((ChatActivity) getActivity())
                    .showChatRoom(room.getConverstionid(), to, room.getCcrecipients());
            //                refreshChatRoom(room);
        }
        //        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        //        if (!mChatRoomsAdapter.isEditionEnabled()) {
        //            mSelectionHelper.enterEditionMode();
        //        }
        //        mChatRoomsAdapter.toggleSelection(position);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //        ContactsManager.getInstance().addContactsListener(this);

        mBackToCallButton.setVisibility(View.INVISIBLE);
        //        Core core = LinphoneManager.getCore();
        //        if (core != null) {
        //            core.addListener(mListener);
        //
        //            if (core.getCallsNb() > 0) {
        //                mBackToCallButton.setVisibility(View.VISIBLE);
        //            }
        //        }
        //
        //        refreshChatRoomsList();

        //        ProxyConfig lpc = core.getDefaultProxyConfig();
        //        mNewGroupDiscussionButton.setVisibility(
        //                (lpc != null && lpc.getConferenceFactoryUri() != null) ? View.VISIBLE :
        // View.GONE);
    }

    @Override
    public void onPause() {
        //        Core core = LinphoneManager.getCore();
        //        if (core != null) {
        //            core.removeListener(mListener);
        //        }
        //        ContactsManager.getInstance().removeContactsListener(this);
        super.onPause();
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        Core core = LinphoneManager.getCore();
        mChatRoomDeletionPendingCount = objectsToDelete.length;
        for (Object obj : objectsToDelete) {
            ChatRoom room = (ChatRoom) obj;
            room.addListener(mChatRoomListener);

            for (EventLog eventLog : room.getHistoryMessageEvents(0)) {
                LinphoneUtils.deleteFileContentIfExists(eventLog);
            }

            core.deleteChatRoom(room);
        }
        if (mChatRoomDeletionPendingCount > 0) {
            mWaitLayout.setVisibility(View.VISIBLE);
        }
        ((ChatActivity) getActivity()).displayMissedChats();

        if (getResources().getBoolean(R.bool.isTablet))
            ((ChatActivity) getActivity()).showEmptyChildFragment();
    }

    @Override
    public void onContactsUpdated() {
        //        ChatRoomsAdapter adapter = (ChatRoomsAdapter) mChatRoomsList.getAdapter();
        //        if (adapter != null) {
        //            adapter.refresh(true);
        //        }
    }

    private void scrollToTop() {
        mChatRoomsList.getLayoutManager().scrollToPosition(0);
    }

    private void refreshChatRoom(ChatRoom cr) {
        //        ChatRoomViewHolder holder = (ChatRoomViewHolder) cr.getUserData();
        //        if (holder != null) {
        //            int position = holder.getAdapterPosition();
        //            if (position == 0) {
        //                mChatRoomsAdapter.notifyItemChanged(0);
        //            } else {
        //                refreshChatRoomsList();
        //            }
        //        } else {
        //            refreshChatRoomsList();
        //        }
    }

    private void refreshChatRoomsList() {
        //        mChatRoomsAdapter.refresh();
        //        mNoChatHistory.setVisibility(
        //                mChatRoomsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
