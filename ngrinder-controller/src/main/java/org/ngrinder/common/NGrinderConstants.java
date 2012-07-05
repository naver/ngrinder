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
package org.ngrinder.common;

import javax.annotation.PostConstruct;

import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class NGrinderConstants {

	private NGrinderConstants() {
		super();
	}

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String END = "end";

	/**
	 * .ngrinder<br>
	 * |project<br>
	 * |--|u_username1<br>
	 * |--|--|s_scriptid1<br>
	 * |--|--|--|script.properties<br>
	 * |--|--|--|histories<br>
	 * |--|--|--|reports<br>
	 * |--|--|--|logs<br>
	 * |--|--|s_scriptid2<br>
	 * |--|--|libs<br>
	 * |--|u_username2<br>
	 * 
	 */

	// project
	public static String PATH_PROJECT;

	@Autowired
	private Config config;

	@PostConstruct
	public void init() {
		PATH_PROJECT = config.getHome().getProjectDirectory().getAbsolutePath();
	}

	public static final String PREFIX_USER = "u_";

	public static final String PATH_LIB = "libs";

	// script
	// public static final String PATH_SCRIPT = getWebRoot() + "scripts";

	public static final String PREFIX_SCRIPT = "s_";

	public static final String SCRIPT_PROPERTIES = "script.properties";

	public static final String PATH_LOG = "logs";

	public static final String PATH_REPORT = "reports";

	public static final String PATH_HISTORY = "histories";

	public static final String CACHE_NAME = "cache";

	public static final String COMMA = ",";

	public static final String ENCODE_UTF8 = "UTF-8";

	public static final String PY_EXTENTION = ".py";
	public static final String JS_EXTENTION = ".js";

	public static final String GRINDER_PROPERTIES = "grinder.properties";

	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MESSAGE = "message";

	public static String getWebRoot() {
		String path = NGrinderConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (path.indexOf("WEB-INF") > 0) {
			path = path.substring(0, path.indexOf("WEB-INF/classes"));
		} else if (path.indexOf("classes") > 0) {
			path = path.substring(0, path.indexOf("classes"));
		}
		return path;
	}
}
