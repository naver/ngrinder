/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.common.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Controller base which is reused widely.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class NGrinderBaseController implements NGrinderConstants {

	public static final String ERROR_PAGE = "errors/error";

	protected static final int DEFAULT_PAGE_LIMIT = 20;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UserContext userContext;

	@PostConstruct
	void initJSON() {
		JsonObject rtnJson = new JsonObject();
		rtnJson.addProperty(JSON_SUCCESS, true);
		successJson = rtnJson.toString();
		rtnJson.addProperty(JSON_SUCCESS, false);
		errorJson = rtnJson.toString();
	}

	/**
	 * Get current user.
	 * 
	 * @return current user
	 */
	public User getCurrentUser() {
		return userContext.getCurrentUser();
	}

	/**
	 * Provide current login user as a model attributes. If it's not found,
	 * return empty user.
	 * 
	 * @return login user
	 */
	@ModelAttribute("currentUser")
	public User currentUser() {
		try {
			return getCurrentUser();
		} catch (AuthenticationCredentialsNotFoundException e) {
			// Fall through
		}
		return new User();
	}

	/**
	 * Provide nGrinder version as a model attributes.
	 * 
	 * @return nGrinder version
	 */
	@ModelAttribute("nGrinderVersion")
	public String nGrinderVersion() {
		return Config.getVerionString();
	}

	/**
	 * Get message from messageSource by key.
	 * 
	 * @param key
	 *            key of message
	 * @return found message. If not found, error message will return.
	 */
	protected String getMessages(String key) {
		Locale locale = null;
		String message = null;
		try {
			locale = new Locale(getCurrentUser().getUserLanguage());
			message = messageSource.getMessage(key, null, locale);
		} catch (Exception e) {
			return "Getting message error for key " + key;
		}
		return message;
	}

	private static String successJson;
	private static String errorJson;
	private static Gson gson = new Gson();

	/**
	 * Return success json message.
	 * @param message message
	 * @return json message
	 */
	public String returnSuccess(String message) {
		JsonObject rtnJson = new JsonObject();
		rtnJson.addProperty(JSON_SUCCESS, true);
		rtnJson.addProperty(JSON_MESSAGE, message);
		return rtnJson.toString();
	}

	/**
	 * Return error json message.
	 * @param message message
	 * @return json message
	 */
	public String returnError(String message) {
		JsonObject rtnJson = new JsonObject();
		rtnJson.addProperty(JSON_SUCCESS, false);
		rtnJson.addProperty(JSON_MESSAGE, message);
		return rtnJson.toString();
	}

	/**
	 * Return raw success json message.
	 * @return json message
	 */
	public String returnSuccess() {
		return successJson;
	}

	/**
	 * Return raw error json message.
	 * @return json message
	 */
	public String returnError() {
		return errorJson;
	}

	/**
	 * Convert the given list into json message.
	 * @param list list
	 * @return json message
	 */
	public String toJson(List<?> list) {
		return gson.toJson(list);
	}

	/**
	 * Convert the given map into json message.
	 * @param map map
	 * @return json message
	 */
	public String toJson(Map<String, Object> map) {
		return gson.toJson(map);
	}

}
