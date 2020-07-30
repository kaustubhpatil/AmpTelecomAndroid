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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.amptelecom.android.app.chat.ChatMessageViewHolderClickListener;
import com.amptelecom.android.app.chat.ChatMessagesGenericAdapter;
import com.amptelecom.android.app.chatnew.ChatMessageFragment;
import com.amptelecom.android.app.chatnew.model.ChatMessage;
import com.amptelecom.android.app.contacts.LinphoneContact;
import com.amptelecom.android.app.utils.SelectableAdapter;
import com.amptelecom.android.app.utils.SelectableHelper;
import java.util.ArrayList;
import java.util.List;
import org.linphone.core.EventLog;

public class ChatMessagesAdapter extends SelectableAdapter<ChatMessageViewHolder>
        implements ChatMessagesGenericAdapter {
    private static final int MAX_TIME_TO_GROUP_MESSAGES = 300; // 5 minutes

    private final Context mContext;
    private final int mItemResource;
    private final ChatMessageFragment mFragment;

    private final List<ChatMessage> mTransientMessages;

    private final ChatMessageViewHolderClickListener mClickListener;

    public ChatMessagesAdapter(
            ChatMessageFragment fragment,
            SelectableHelper helper,
            int itemResource,
            List<ChatMessage> messages,
            ChatMessageViewHolderClickListener clickListener) {
        super(helper);
        mFragment = fragment;
        mContext = mFragment.getActivity();
        mItemResource = itemResource;
        mClickListener = clickListener;
        mTransientMessages = messages;
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mItemResource, parent, false);
        ChatMessageViewHolder VH = new ChatMessageViewHolder(mContext, v, mClickListener);

        // Allows onLongClick ContextMenu on bubbles
        mFragment.registerForContextMenu(v);
        v.setTag(VH);
        return VH;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        if (position < 0) return;

        holder.rightAnchor.setVisibility(View.GONE);
        holder.bubbleLayout.setVisibility(View.GONE);
        holder.sendInProgress.setVisibility(View.GONE);

        holder.isEditionEnabled = isEditionEnabled();

        ChatMessage message = mTransientMessages.get(position);

        holder.bindMessage(message);
    }

    public void addItem(ChatMessage chatMessage) {
        mTransientMessages.add(0, chatMessage);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTransientMessages.size();
    }

    @Override
    public void addToHistory(EventLog log) {}

    @Override
    public void addAllToHistory(ArrayList<EventLog> logs) {}

    @Override
    public void setContacts(ArrayList<LinphoneContact> participants) {}

    @Override
    public void refresh(EventLog[] history) {}

    @Override
    public void clear() {}

    @Override
    public void removeItem(int i) {}

    @Override
    public boolean removeFromHistory(EventLog eventLog) {
        return false;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
}
