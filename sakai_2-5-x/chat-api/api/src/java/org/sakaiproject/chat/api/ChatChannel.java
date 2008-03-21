/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.chat.api;

import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageChannel;

/**
 * <p>
 * ChatChannel is the extension to the MessageChanel interface for a Sakai Chat service chat channel. Messages in the ChatChannel are ChatMessages with ChatMessageHeaders.
 * </p>
 * <p>
 * Security is defined, see MessageChannel.
 * </p>
 * <p>
 * Usage Events generated:
 * <ul>
 * <li>chat.message.channel.read - chat message resource id</li>
 * <li>chat.message.channel.remove.any - chat message resource id</li>
 * <li>chat.message.channel.remove.own - chat message resource id</li>
 * <li>chat.message.channel.post - chat message resource id</li>
 * </p>
 */
public interface ChatChannel extends MessageChannel
{
	/**
	 * A (ChatMessage) cover for getMessage to return a specific chat channel message, as specified by message id.
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the ChatMessage that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this chat channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 */
	public ChatMessage getChatMessage(String messageId) throws IdUnusedException, PermissionException;

	/**
	 * A (ChatMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the Message that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 * @exception InUseException
	 *            if the current user does not have permission to mess with this user.
	 */
	public ChatMessageEdit editChatMessage(String messageId) throws IdUnusedException, PermissionException, InUseException;

	/**
	 * A (ChatMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @return The newly added message, locked for update.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	public ChatMessageEdit addChatMessage() throws PermissionException;

	/**
	 * a (ChatMessage) cover for addMessage to add a new message to this channel.
	 * 
	 * @param attachments
	 *        The message header attachments, a vector of Reference objects.
	 * @param body
	 *        The body text.
	 * @return The newly added message.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	public ChatMessage addChatMessage(List attachments, String body) throws PermissionException;
}
