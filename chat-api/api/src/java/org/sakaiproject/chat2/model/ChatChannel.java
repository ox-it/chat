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
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChatChannel implements Entity {
   
   /** Message filter names */
   public static final String FILTER_BY_NUMBER = "SelectMessagesByNumber";
   public static final String FILTER_BY_TIME = "SelectMessagesByTime";
   public static final String FILTER_TODAY = "SelectTodaysMessages";
   public static final String FILTER_ALL = "SelectAllMessages";
   
   private String id;
   private String context;
   private Date creationDate;
   private String title;
   private String description;
   private String filterType = FILTER_BY_TIME;
   private int filterParam = 3;
   private boolean contextDefaultChannel = false;
   private Set messages = new HashSet();
   
   
   public ChatChannel() {
   }
   
   public String getContext() {
      return context;
   }
   public void setContext(String context) {
      this.context = context;
   }
   public Date getCreationDate() {
      return creationDate;
   }
   public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
   }
   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }
   public String getTitle() {
      return title;
   }
   public void setTitle(String title) {
      this.title = title;
   }
   public String getDescription() {
      return description;
   }
   public void setDescription(String description) {
      this.description = description;
   }
   public Set getMessages() {
      return messages;
   }
   public void setMessages(Set messages) {
      this.messages = messages;
   }
   public String getFilterType() {
      return filterType;
   }
   public void setFilterType(String filterType) {
      this.filterType = filterType;
   }
   public int getFilterParam() {
      return filterParam;
   }
   public void setFilterParam(int filterParam) {
      this.filterParam = filterParam;
   }
   public boolean isContextDefaultChannel() {
      return contextDefaultChannel;
   }
   public void setContextDefaultChannel(boolean contextDefaultChannel) {
      this.contextDefaultChannel = contextDefaultChannel;
   }
   
   /**
    * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
    * 
    * @param doc
    *        The DOM doc to contain the XML (or null for a string return).
    * @param stack
    *        The DOM elements, the top of which is the containing element of the new "resource" element.
    * @return The newly added element.
    */
   public Element toXml(Document doc, Stack stack)
   {
      Element channel = doc.createElement("channel");

      if (stack.isEmpty())
      {
         doc.appendChild(channel);
      }
      else
      {
         ((Element) stack.peek()).appendChild(channel);
      }

      stack.push(channel);

      channel.setAttribute("context", getContext());
      channel.setAttribute("id", getId());
      channel.setAttribute("description", getDescription());
      channel.setAttribute("title", getTitle());
      channel.setAttribute("creationDate", Long.toString(getCreationDate().getTime()));
      channel.setAttribute("filterType", getFilterType());
      channel.setAttribute("filterParam", Integer.toString(getFilterParam()));
      channel.setAttribute("contextDefaultChannel", Boolean.toString(isContextDefaultChannel()));
      
      stack.pop();

      return channel;

   } // toXml
   
   /**
    * Converts the serialized xml back to a ChatChannel object
    * @param channelElement
    * @return
    */
   public static ChatChannel xmlToChatChannel(Element channelElement, String siteId) {
      ChatChannel tmpChannel = new ChatChannel();
      //tmpChannel.setContext(channelElement.getAttribute("context"));
      tmpChannel.setContext(siteId);
      
      if (siteId.equalsIgnoreCase(channelElement.getAttribute("context"))) {
         //If importing into the same site, keep the id.  We should get an update instead of saving a new one.
         tmpChannel.setId(channelElement.getAttribute("id"));
      }
      tmpChannel.setDescription(channelElement.getAttribute("description"));
      tmpChannel.setTitle(channelElement.getAttribute("title"));
      tmpChannel.setCreationDate(new Date(Long.parseLong(channelElement.getAttribute("creationDate"))));
      tmpChannel.setFilterType(channelElement.getAttribute("filterType"));
      tmpChannel.setFilterParam(Integer.parseInt(channelElement.getAttribute("filterParam")));
      tmpChannel.setContextDefaultChannel(Boolean.parseBoolean(channelElement.getAttribute("contextDefaultChannel")));
      
      return tmpChannel;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getProperties()
    */
   public ResourceProperties getProperties() {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getReference()
    */
   public String getReference() {
      return ChatManager.REFERENCE_ROOT + Entity.SEPARATOR + ChatManager.REF_TYPE_CHANNEL + Entity.SEPARATOR + context + Entity.SEPARATOR + id;

   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
    */
   public String getReference(String rootProperty) {
      return getReference();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl()
    */
   public String getUrl() {
      return ServerConfigurationService.getAccessUrl() + getReference();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
    */
   public String getUrl(String rootProperty) {
      return getUrl();
   }
}
