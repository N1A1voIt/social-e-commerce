package com.itu.socialcom.demo.messages.inbox;

import com.itu.socialcom.demo.messages.messagemother.MessageMotherCPL;
import com.itu.socialcom.demo.socialmedia.entity.ManagedPageCPL;
import lombok.Data;

import java.util.List;

@Data
public class InboxDisplay {
    ManagedPageCPL page;
    List<MessageMotherCPL> mothers;
}