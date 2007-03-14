/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.chat2.model;

import java.util.Date;
import java.util.List;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.exception.PermissionException;



/**
 * 
 * @author andersjb
 *
 */
public interface ChatManager extends EntityProducer, EntitySummary, EntityTransferrer {

   /** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
   static final String APPLICATION_ID = "sakai:chat";
   
   /** This string starts the references to resources in this service. */
   public static final String REFERENCE_ROOT = "/chat";
   public static final String REF_TYPE_CHANNEL = "channel";
   
   /** The Reference type for a messgae. */
   public static final String REF_TYPE_MESSAGE = "msg";

   public static final String CHAT_TOOL_ID = "sakai.chat";
   
   /**
    * Creates a new ChatChannel but doesn't put it in the database.
    * @param context Id of what the channel is linked to
    * @param title String the title of the channel
    * @param contextDefaultChannel boolean to set this as the default channel in the context
    * @param checkAuthz boolean indicating if we should check for authorization before creating the channel
    * @return ChatChannel the new un-saved channel
    */
   public ChatChannel createNewChannel(String context, String title, boolean contextDefaultChannel, boolean checkAuthz) throws PermissionException;
   
   /**
    * updates the channel back into the database
    * @param channel ChatChannel
    * @param checkAuthz boolean indicating if we should check for authorization before updating
    */
   public void updateChannel(ChatChannel channel, boolean checkAuthz) throws PermissionException;
   
   /**
    * deletes the channel from the database.  It also removes the ChatMessages
    * @param channel
    */
   public void deleteChannel(ChatChannel channel) throws PermissionException;

   /**
    * gets one chat room
    * @param chatChannelId Id
    * @return ChatChannel
    */
   public ChatChannel getChatChannel(String chatChannelId);

   /**
    * gets all the messages from the Channel after the passed date
    * @param channel ChatChannel 
    * @param context Context of channel and messages to return
    * @param date Date that the messages need to be newer than.  All messages will be returned if null
    * @param items The number of messages to return.  All if set to 0
    * @param sortAsc Boolean to sort the records in ascending order
    * @return List of ChatMessages
    */
   public List<ChatMessage> getChannelMessages(ChatChannel channel, String context, Date date, int items, boolean sortAsc) throws PermissionException;
   
   /**
    * creates an unsaved Chat Message
    * @param ChatChannel the channel that the new message will be in
    * @param String  the owner of the message
    * @return ChatMessage
    */
   public ChatMessage createNewMessage(ChatChannel channel, String owner) throws PermissionException;
   
   /**
    * saves a Chat Message
    * @param ChatMessage the message to update
    */
   public void updateMessage(ChatMessage message);
   
   /**
    * delete a Chat Message
    * @param ChatMessage the message to delete
    */
   public void deleteMessage(ChatMessage message) throws PermissionException;
   
   /**
    * gets the message with the id
    * @param chatMessageId Id
    * @return ChatMessage
    */
   public ChatMessage getMessage(String chatMessageId);

   /**
    * Adds a room listener on the room
    * @param observer RoomObserver the class to observe the room
    * @param roomId the room being observed
    */
   public void addRoomListener(RoomObserver observer, String roomId);

   /**
    * Removes a room listener on the room
    * @param observer RoomObserver the class to stop observing the room
    * @param roomId the room being observed
    */
   public void removeRoomListener(RoomObserver observer, String roomId);
   
   /**
    * sends the message out to the other clients
    * @param entry ChatMessage
    */
   public void sendMessage(ChatMessage entry);
   
   /**
    * gets the rooms associated with the context
    * @param contextId Id
    * @param defaultNewTitle String the default name of a new ChatChannel
    * @return List of ChatChannel
    */
   public List getContextChannels(String contextId, String defaultNewTitle);
   
   /**
    * Returns the context's default channel, or null if none.
    * @param contextId
    * @return
    */
   public ChatChannel getDefaultChannel(String contextId);


   public boolean getCanDelete(ChatMessage chatMessage);
   public boolean getCanDelete(ChatMessage message, String placementId);
   
   public boolean getCanDelete(ChatChannel channel);
   //public boolean getCanDelete(ChatChannel channel, String placementId);
   
   public boolean getCanEdit(ChatChannel channel);
   
   public boolean getCanCreateChannel();
   public boolean getCanReadMessage(ChatChannel channel);
   
   public boolean isMaintainer();
   
   /**
    * Makes the passed channel the dfault in the channel's context
    * @param channel
    */
   public void makeDefaultContextChannel(ChatChannel channel);
   
   /**
    * Returns a Date object that is the offset number of days before the current date
    * @param offset Difference in days from current date
    * @return
    */
   public Date calculateDateByOffset(int offset);
   
}
