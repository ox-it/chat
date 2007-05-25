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

/**
 * 
 */
package org.sakaiproject.chat2.model.impl;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * @author chrismaurer
 *
 */
public class ChatDataMigration { 
   protected final transient Log logger = LogFactory.getLog(getClass());
   
   private boolean debug = false;
   
   private SqlService sqlService = null;
   
   private Statement stmt;
   
   private ResourceLoader toolBundle;
   
   private boolean performChatMigration = false;
   private boolean chatMigrationExecuteImmediate = true;
   
   
   /** init thread - so we don't wait in the actual init() call */
   public class ChatDataMigrationThread extends Thread
   {
      /**
       * construct and start the init activity
       */
      public ChatDataMigrationThread()
      {
         start();
      }

      /**
       * run the init
       */
      public void run()
      {
         try {
            load();
         } catch (Exception e) {
            logger.warn("Error with ChatDataMigrationThread.run()", e);
         }
      }
   }
   
   /**
    * Called on after the startup of the singleton.  This sets the global
    * list of functions which will have permission managed by sakai
    * @throws Exception
    */
   protected void init() throws Exception
   {
      logger.info("init()");
      
      try {         
         if (performChatMigration) {
            new ChatDataMigrationThread();
            //load();
         }         
      }
      catch (Exception e) {
         logger.warn("Error with ChatDataMigration.init()", e);
      }
      
   }
   
   /**
    * Destroy
    */
   public void destroy()
   {
      logger.info("destroy()");
   }
   
   public void load() throws Exception {
      logger.info("Running Chat Migration; immediate: " + chatMigrationExecuteImmediate);
      Connection connection = null;
      
      try {
         
         connection = sqlService.borrowConnection();
         printDebug("*******BORROWED A CONNECTION");
         runChannelMigration(connection);
         
         runMessageMigration(connection);
         
      }
      catch (Exception e) {
         printDebug(e.toString());
         logger.error(e.getMessage());
         throw new Exception(e);         
      }
      finally {
         if (connection != null) {
            try {
               sqlService.returnConnection(connection);
            }
            catch (Exception e) {
               // can't do anything with this.
            }
         }
      }
      logger.info("Chat migration complete");
   }
   
   protected void runChannelMigration(Connection con) {
      logger.debug("runChannelMigration()");
      printDebug("*******GETTING CHANNELS");
      
      String sql = getMessageFromBundle("select.oldchannels");
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /* 
                   * CHANNEL_ID
                   * XML
                   */
                  String oldId = rs.getString("CHANNEL_ID");
                  Object xml = rs.getObject("XML");
                  
                  printDebug("*******FOUND CHANNEL: " + oldId);
                  printDebug("*******FOUND CHANNEL: " + xml);
                  
                  Document doc = null;
                  try {
                     doc = Xml.readDocumentFromString((String)xml);
                  }
                  catch (ClassCastException cce) {
                     Clob xmlClob = (Clob) xml;
                     doc = Xml.readDocumentFromStream(xmlClob.getAsciiStream());
                  }

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String context = root.getAttribute("context");
                  String title = root.getAttribute("id");
                  String newChannelId = escapeSpecialCharsForId(oldId);
                  
                  //TODO Chat lookup the config params?
                  
                  String runSql = getMessageFromBundle("insert.channel");
                  Object[] fields = new Object[] {newChannelId, context, null, title, "", "SelectMessagesByTime", 3, 0, 1, oldId, oldId};
                  
                  
                  /* 
                   * CHANNEL_ID, 
                   * CONTEXT, 
                   * CREATION_DATE, 
                   * title, 
                   * description, 
                   * filterType, 
                   * filterParam, 
                   * contextDefaultChannel, 
                   * migratedChannelId,
                   * ENABLE_USER_OVERRIDE
                   */
                  
                  if (chatMigrationExecuteImmediate) {
                     sqlService.dbWrite(null, runSql, fields);
                  }
               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        logger.debug("Migration task fininshed: runChannelMigration()");
   }
   
   protected void runMessageMigration(Connection con) {
      logger.debug("runMessageMigration()");
      printDebug("*******GETTING MESSAGES");
      
      String sql = getMessageFromBundle("select.oldmessages");
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /*
                   * MESSAGE_ID
                   * CHANNEL_ID                            
                   * XML
                   * OWNER                          
                   * MESSAGE_DATE
                   */
                  String oldMessageId = rs.getString("MESSAGE_ID");
                  String oldChannelId = rs.getString("CHANNEL_ID");
                  Object xml = rs.getObject("XML");
                  String owner = rs.getString("OWNER");
                  // Date messageDate = rs.getTimestamp("MESSAGE_DATE");
                  Time messageDate = TimeService.newTime(rs.getTimestamp("MESSAGE_DATE").getTime());
                  
                  printDebug("*******FOUND MESSAGE: " + oldMessageId);
                  printDebug("*******FOUND MESSAGE: " + xml);
                  
                  Document doc = null;
                  try {
                     doc = Xml.readDocumentFromString((String)xml);
                  }
                  catch (ClassCastException cce) {
                     Clob xmlClob = (Clob) xml;
                     doc = Xml.readDocumentFromStream(xmlClob.getAsciiStream());
                  }

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String body = Xml.decodeAttribute(root, "body");
                  
                  String newMessageId = oldMessageId;
                  String runSql = getMessageFromBundle("insert.message");
                  
                  Object[] fields = new Object[] {
                        escapeSpecialCharsForId(newMessageId), escapeSpecialCharsForId(oldChannelId), owner, messageDate, body, oldMessageId, oldMessageId};
                  
                  /*
                   * insert into CHAT2_MESSAGE (MESSAGE_ID, CHANNEL_ID, OWNER, MESSAGE_DATE, BODY) \
                        values ('{0}', '{1}', '{2}', '{3}', '{4}');
                  
                  */
                  
                  if (chatMigrationExecuteImmediate) {
                     sqlService.dbWrite(null, runSql, fields);
                  }
               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        logger.debug("Migration task fininshed: runMessageMigration()");
      
   }

   /**
    * Escapes special characters that may be bad in sql statements
    * -- "/" is replaced with "_"
    * @param input Original string to parse
    * @return A string with any special characters escaped
    */
   protected String escapeSpecialCharsForId(String input) {
      String output = input.replaceAll("/", "_");
      return output;
   }
   
   
   protected void printDebug(String message) {
      if (debug) {
         //System.out.println(message);
         //logger.debug(message);
         logger.info("DEBUG: " + message);
      }
   }
   
   
   /**
    * Looks up the sql statements defined in chat-sql.properties.  
    * @param key
    * @return
    */
   private String getMessageFromBundle(String key) {
      if (toolBundle == null)
         toolBundle = new ResourceLoader("chat-sql");
      
      return toolBundle.getString(key);
   }
      
   public boolean isChatMigrationExecuteImmediate() {
      return chatMigrationExecuteImmediate;
   }

   public void setChatMigrationExecuteImmediate(
         boolean chatMigrationExecuteImmediate) {
      this.chatMigrationExecuteImmediate = chatMigrationExecuteImmediate;
   }

   public boolean isPerformChatMigration() {
      return performChatMigration;
   }

   public void setPerformChatMigration(boolean performChatMigration) {
      this.performChatMigration = performChatMigration;
   }

   public boolean isDebug() {
      return debug;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public SqlService getSqlService() {
      return sqlService;
   }

   public void setSqlService(SqlService sqlService) {
      this.sqlService = sqlService;
   }
   
}
