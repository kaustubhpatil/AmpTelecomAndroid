/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.amptelecom.android.app.chatnew.adapter;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.chat.ChatActivity;
import com.amptelecom.android.app.chat.ChatMessageViewHolderClickListener;
import com.amptelecom.android.app.chatnew.model.ChatMessage;
import com.amptelecom.android.app.utils.FileUtils;
import com.amptelecom.android.app.utils.ImageUtils;
import com.amptelecom.android.app.utils.LinphoneUtils;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.linphone.core.Content;
import org.linphone.core.tools.Log;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public final View rightAnchor;
    public final RelativeLayout bubbleLayout;
    public final LinearLayout background;
    public final RelativeLayout avatarLayout;

    private final ProgressBar downloadInProgress;
    public final ProgressBar sendInProgress;
    public final TextView timeText;
    private final TextView messageText;

    private final FlexboxLayout multiFileContents;
    private final RelativeLayout singleFileContent;

    private CountDownTimer countDownTimer;

    public boolean isEditionEnabled;

    private Context mContext;
    private ChatMessageViewHolderClickListener mListener;

    public ChatMessageViewHolder(
            Context context, View view, ChatMessageViewHolderClickListener listener) {
        this(view);
        mContext = context;
        mListener = listener;
        view.setOnClickListener(this);
    }

    private ChatMessageViewHolder(View view) {
        super(view);

        rightAnchor = view.findViewById(R.id.rightAnchor);
        bubbleLayout = view.findViewById(R.id.bubble);
        background = view.findViewById(R.id.background);
        avatarLayout = view.findViewById(R.id.avatar_layout);

        downloadInProgress = view.findViewById(R.id.download_in_progress);
        sendInProgress = view.findViewById(R.id.send_in_progress);
        timeText = view.findViewById(R.id.time);
        messageText = view.findViewById(R.id.message);

        singleFileContent = view.findViewById(R.id.single_content);
        multiFileContents = view.findViewById(R.id.multi_content);

        countDownTimer = null;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClicked(getAdapterPosition());
        }
    }

    public void bindMessage(final ChatMessage message) {

        rightAnchor.setVisibility(View.VISIBLE);
        bubbleLayout.setVisibility(View.VISIBLE);
        messageText.setVisibility(View.GONE);
        timeText.setVisibility(View.VISIBLE);
        avatarLayout.setVisibility(View.GONE);
        sendInProgress.setVisibility(View.GONE);
        downloadInProgress.setVisibility(View.GONE);
        singleFileContent.setVisibility(View.GONE);
        multiFileContents.setVisibility(View.GONE);

        //        ChatMessage.State status = message.getState();
        //        Address remoteSender = message.getFromAddress();
        String displayName;
        //        String time =
        //                LinphoneUtils.timestampToHumanDate(
        //                        mContext, message.getTime(), R.string.messages_date_format);

        String time = covertTimeToText(message.getMsgcreated());

        if (!message.getDirection().equals("inbound")) {
            bubbleLayout.setPadding(0, 0, 0, 0); // Reset padding
            //             if (status == ChatMessage.State.InProgress
            //                    || status == ChatMessage.State.FileTransferInProgress) {
            //                sendInProgress.setVisibility(View.VISIBLE);
            //            }

            timeText.setVisibility(View.VISIBLE);
            background.setBackgroundResource(R.drawable.chat_bubble_outgoing_full);
        } else {
            rightAnchor.setVisibility(View.GONE);
            avatarLayout.setVisibility(View.VISIBLE);
            background.setBackgroundResource(R.drawable.chat_bubble_incoming_full);

            // Can't anchor incoming messages, setting this to align max width with LIME icon
            bubbleLayout.setPadding(0, 0, (int) ImageUtils.dpToPixels(mContext, 18), 0);

            //            if (status == ChatMessage.State.FileTransferInProgress) {
            //                downloadInProgress.setVisibility(View.VISIBLE);
            //            }
        }

        //        if (contact == null) {
        //            contact = ContactsManager.getInstance().findContactFromAddress(remoteSender);
        //        }
        //        if (contact != null) {
        //            if (contact.getFullName() != null) {
        //                displayName = contact.getFullName();
        //            } else {
        //                displayName = LinphoneUtils.getAddressDisplayName(remoteSender);
        //            }
        //            ContactAvatar.displayAvatar(contact, avatarLayout);
        //        } else {
        //            displayName = LinphoneUtils.getAddressDisplayName(remoteSender);
        //            ContactAvatar.displayAvatar(displayName, avatarLayout);
        //        }

        if (!message.getDirection().equals("inbound")) {
            timeText.setText(time);
        } else {
            timeText.setText(time + " - " + message.getMsg_from());
        }

        if (message.getMessage() != null && message.getMessage().trim().length() > 0) {
            String msg = message.getMessage();
            Spanned text = LinphoneUtils.getTextWithHttpLinks(msg);
            messageText.setText(text);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            messageText.setVisibility(View.VISIBLE);
        }

        //        List<Content> fileContents = new ArrayList<>();
        //        for (Content c : message.getContents()) {
        //            if (c.isFile() || c.isFileTransfer()) {
        //                fileContents.add(c);
        //            }
        //        }

        //        if (fileContents.size() == 1) {
        //            singleFileContent.setVisibility(View.VISIBLE);
        //            displayContent(message, fileContents.get(0), singleFileContent, false);
        //        } else if (fileContents.size() > 1) {
        //            multiFileContents.removeAllViews();
        //            multiFileContents.setVisibility(View.VISIBLE);
        //
        //            for (Content c : fileContents) {
        //                View content =
        //                        LayoutInflater.from(mContext)
        //                                .inflate(R.layout.chat_bubble_content, null, false);
        //
        //                displayContent(message, c, content, true);
        //
        //                multiFileContents.addView(content);
        //            }
        //        }
    }

    public String covertTimeToText(String dataDate) {

        String convTime = null;

        String prefix = "";
        String suffix = "Ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + " Seconds " + suffix;
            } else if (minute < 60) {
                convTime = minute + " Minutes " + suffix;
            } else if (hour < 24) {
                convTime = hour + " Hours " + suffix;
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " Years " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " Months " + suffix;
                } else {
                    convTime = (day / 7) + " Week " + suffix;
                }
            } else if (day < 7) {
                convTime = day + " Days " + suffix;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            //            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;
    }

    private void displayContent(
            final ChatMessage message, Content c, View content, boolean isMultiContent) {
        final Button downloadOrCancel = content.findViewById(R.id.download);
        downloadOrCancel.setVisibility(View.GONE);
        final ImageView bigImage = content.findViewById(R.id.bigImage);
        bigImage.setVisibility(View.GONE);
        final ImageView smallImage = content.findViewById(R.id.image);
        smallImage.setVisibility(View.GONE);
        final TextView fileName = content.findViewById(R.id.file);
        fileName.setVisibility(View.GONE);

        if (c.isFile() || (c.isFileTransfer() && !c.getFilePath().isEmpty())) {
            // If message is outgoing, even if content
            // is file transfer we have the file available
            final String filePath = c.getFilePath();

            View v;
            if (FileUtils.isExtensionImage(filePath)) {
                if (!isMultiContent
                        && mContext.getResources()
                                .getBoolean(
                                        R.bool.use_big_pictures_to_preview_images_file_transfers)) {
                    loadBitmap(c.getFilePath(), bigImage);
                    v = bigImage;
                } else {
                    loadBitmap(c.getFilePath(), smallImage);
                    v = smallImage;
                }
            } else {
                fileName.setText(FileUtils.getNameFromFilePath(filePath));
                v = fileName;
            }
            v.setVisibility(View.VISIBLE);
            v.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isEditionEnabled) {
                                ChatMessageViewHolder.this.onClick(v);
                            } else {
                                openFile(filePath);
                            }
                        }
                    });
        } else {
            downloadOrCancel.setVisibility(View.VISIBLE);

            if (mContext.getPackageManager()
                            .checkPermission(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    mContext.getPackageName())
                    == PackageManager.PERMISSION_GRANTED) {
                String filename = c.getName();
                File file = new File(FileUtils.getStorageDirectory(mContext), filename);

                int prefix = 1;
                while (file.exists()) {
                    file =
                            new File(
                                    FileUtils.getStorageDirectory(mContext),
                                    prefix + "_" + filename);
                    Log.w(
                            "[Chat Message View] File with that name already exists, renamed to "
                                    + prefix
                                    + "_"
                                    + filename);
                    prefix += 1;
                }
                c.setFilePath(file.getPath());

                downloadOrCancel.setTag(c);
                //                if (!message.isFileTransferInProgress()) {
                //                    downloadOrCancel.setText(R.string.download_file);
                //                } else {
                //                    downloadOrCancel.setText(R.string.cancel);
                //                }

                downloadOrCancel.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isEditionEnabled) {
                                    ChatMessageViewHolder.this.onClick(v);
                                } else {
                                    Content c = (Content) v.getTag();
                                    //                                    if
                                    // (!message.isFileTransferInProgress()) {
                                    //
                                    // message.downloadContent(c);
                                    //                                    } else {
                                    //
                                    // message.cancelFileTransfer();
                                    //                                    }
                                }
                            }
                        });
            } else {
                Log.w(
                        "[Chat Message View] WRITE_EXTERNAL_STORAGE permission not granted, won't be able to store the downloaded file");
                ((ChatActivity) mContext)
                        .requestPermissionIfNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void openFile(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file;
        Uri contentUri;
        if (path.startsWith("file://")) {
            path = path.substring("file://".length());
            file = new File(path);
            contentUri =
                    FileProvider.getUriForFile(
                            mContext,
                            mContext.getResources().getString(R.string.file_provider),
                            file);
        } else if (path.startsWith("content://")) {
            contentUri = Uri.parse(path);
        } else {
            file = new File(path);
            try {
                contentUri =
                        FileProvider.getUriForFile(
                                mContext,
                                mContext.getResources().getString(R.string.file_provider),
                                file);
            } catch (Exception e) {
                Log.e(
                        "[Chat Message View] Couldn't get URI for file "
                                + file
                                + " using file provider "
                                + mContext.getResources().getString(R.string.file_provider));
                contentUri = Uri.parse(path);
            }
        }

        String filePath = contentUri.toString();
        Log.i("[Chat Message View] Trying to open file: " + filePath);
        String type = null;
        String extension = FileUtils.getExtensionFromFileName(filePath);

        if (extension != null && !extension.isEmpty()) {
            Log.i("[Chat Message View] Found extension " + extension);
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        } else {
            Log.e("[Chat Message View] Couldn't find extension");
        }

        if (type != null) {
            Log.i("[Chat Message View] Found matching MIME type " + type);
        } else {
            type = FileUtils.getMimeFromFile(filePath);
            Log.e(
                    "[Chat Message View] Can't get MIME type from extension: "
                            + extension
                            + ", will use "
                            + type);
        }

        intent.setDataAndType(contentUri, type);
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);

        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Log.e("[Chat Message View] Couldn't find an activity to handle MIME type: " + type);
            Toast.makeText(mContext, R.string.cant_open_file_no_app_found, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void loadBitmap(String path, ImageView imageView) {
        Glide.with(mContext).load(path).into(imageView);
    }

    private String formatLifetime(long seconds) {
        long days = seconds / 86400;
        if (days == 0) {
            return String.format(
                    "%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
        } else {
            return mContext.getResources().getQuantityString(R.plurals.days, (int) days, days);
        }
    }
}
