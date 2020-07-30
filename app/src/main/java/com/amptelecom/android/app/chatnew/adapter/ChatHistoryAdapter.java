package com.amptelecom.android.app.chatnew.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DiffUtil;
import com.amptelecom.android.app.chatnew.model.ChatData;
import com.amptelecom.android.app.utils.SelectableAdapter;
import com.amptelecom.android.app.utils.SelectableHelper;
import java.util.List;
import org.linphone.core.ChatRoom;

public class ChatHistoryAdapter extends SelectableAdapter<ChatHistoryViewHolder> {
    private final Context mContext;
    private List<ChatData> mRooms;
    private final int mItemResource;
    private final ChatHistoryViewHolder.ClickListener mClickListener;

    public ChatHistoryAdapter(
            Context context,
            int itemResource,
            List<ChatData> rooms,
            ChatHistoryViewHolder.ClickListener clickListener,
            SelectableHelper helper) {
        super(helper);
        mClickListener = clickListener;
        mRooms = rooms;
        mContext = context;
        mItemResource = itemResource;
    }

    @Override
    public ChatHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mItemResource, parent, false);
        return new ChatHistoryViewHolder(mContext, view, mClickListener);
    }

    @Override
    public void onBindViewHolder(ChatHistoryViewHolder holder, int position) {
        ChatData room = mRooms.get(position);

        //        room.setUserData(holder);
        holder.bindChatRoom(room);
    }

    public void refresh() {
        //        refresh(false);
    }

    //    public void refresh(boolean force) {
    //        ChatRoom[] rooms = LinphoneManager.getCore().getChatRooms();
    //        List<ChatRoom> roomsList = Arrays.asList(rooms);
    //
    //        if (!force) {
    //            DiffUtil.DiffResult diffResult =
    //                    DiffUtil.calculateDiff(new
    // ChatRoomsAdapter.ChatRoomDiffCallback(roomsList, mRooms));
    //            diffResult.dispatchUpdatesTo(this);
    //            mRooms = roomsList;
    //        } else {
    //            mRooms = roomsList;
    //            notifyDataSetChanged();
    //        }
    //    }

    /** Adapter's methods */
    @Override
    public int getItemCount() {
        return mRooms.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mRooms.size()) return mRooms.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ChatRoomDiffCallback extends DiffUtil.Callback {
        List<ChatRoom> oldChatRooms;
        List<ChatRoom> newChatRooms;

        public ChatRoomDiffCallback(List<ChatRoom> newRooms, List<ChatRoom> oldRooms) {
            oldChatRooms = oldRooms;
            newChatRooms = newRooms;
        }

        @Override
        public int getOldListSize() {
            return oldChatRooms.size();
        }

        @Override
        public int getNewListSize() {
            return newChatRooms.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldChatRooms.get(oldItemPosition) == (newChatRooms.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return newChatRooms.get(newItemPosition).getUnreadMessagesCount() == 0;
        }
    }
}
