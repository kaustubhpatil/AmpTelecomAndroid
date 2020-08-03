package com.amptelecom.android.app.chatnew.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.amptelecom.android.app.R;
import com.amptelecom.android.app.chatnew.model.ChatData;
import com.amptelecom.android.app.contacts.ContactsManager;
import com.amptelecom.android.app.contacts.LinphoneContact;
import com.amptelecom.android.app.utils.LinphoneUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.linphone.core.Address;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ChatRoomCapabilities;

public class ChatHistoryViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private final TextView lastMessageView;
    private final TextView date;
    private final TextView displayName;

    private final Context mContext;
    private final ChatHistoryViewHolder.ClickListener mListener;

    public ChatHistoryViewHolder(
            Context context, View itemView, ChatHistoryViewHolder.ClickListener listener) {
        super(itemView);

        mContext = context;
        lastMessageView = itemView.findViewById(R.id.lastMessage);
        date = itemView.findViewById(R.id.date);
        displayName = itemView.findViewById(R.id.sipUri);
        mListener = listener;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindChatRoom(ChatData room) {
        if (room.getMessage() != null && room.getMessage().trim().length() > 0) {
            lastMessageView.setText(room.getMessage());
        } else {
            lastMessageView.setText("Image");
        }

        date.setText(covertTimeToText(room.getMsgcreated()));
        //        date.setText("22 seconds ago");

        if (room.getDirection().equals("inbound")) {
            if (room.getCcrecipients() != null && room.getCcrecipients().size() > 0) {
                displayName.setText(
                        room.getFrom() + "," + TextUtils.join(",", room.getCcrecipients()));
            } else {
                displayName.setText(room.getFrom());
            }

        } else {
            if (room.getCcrecipients() != null && room.getCcrecipients().size() > 0) {
                displayName.setText(
                        room.getTo() + "," + TextUtils.join(",", room.getCcrecipients()));
            } else {
                displayName.setText(room.getTo());
            }
        }
        //        displayName.setText(getContact(room));

    }

    public String covertTimeToText(String dataDate) {

        String convTime = null;

        String prefix = "";
        //        String suffix = "Ago";

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
                convTime = second + " Seconds";
            } else if (minute < 60) {
                convTime = minute + " Minutes";
            } else if (hour < 24) {
                convTime = hour + " Hours";
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " Years";
                } else if (day > 30) {
                    convTime = (day / 30) + " Months";
                } else {
                    convTime = (day / 7) + " Week";
                }
            } else if (day < 7) {
                convTime = day + " Days";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            //            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;
    }

    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClicked(getAdapterPosition());
        }
    }

    public boolean onLongClick(View v) {
        if (mListener != null) {
            return mListener.onItemLongClicked(getAdapterPosition());
        }
        return false;
    }

    private String getSender(ChatMessage lastMessage) {
        if (lastMessage != null) {
            LinphoneContact contact =
                    ContactsManager.getInstance()
                            .findContactFromAddress(lastMessage.getFromAddress());
            if (contact != null) {
                return (contact.getFullName() + mContext.getString(R.string.separator));
            }
            return (LinphoneUtils.getAddressDisplayName(lastMessage.getFromAddress())
                    + mContext.getString(R.string.separator));
        }
        return null;
    }

    private String getContact(ChatRoom mRoom) {
        Address contactAddress = mRoom.getPeerAddress();
        if (mRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())
                && mRoom.getParticipants().length > 0) {
            contactAddress = mRoom.getParticipants()[0].getAddress();
        }

        if (mRoom.hasCapability(ChatRoomCapabilities.OneToOne.toInt())) {
            LinphoneContact contact;
            contact = ContactsManager.getInstance().findContactFromAddress(contactAddress);
            if (contact != null) {
                return contact.getFullName();
            }
            return LinphoneUtils.getAddressDisplayName(contactAddress);
        }
        return mRoom.getSubject();
    }

    public interface ClickListener {
        void onItemClicked(int position);

        boolean onItemLongClicked(int position);
    }
}
