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
package org.ngrinder.script.service.impl;

import java.util.List;

import org.ngrinder.script.model.Script;
import org.ngrinder.script.repository.ScriptDao;
import org.ngrinder.script.service.ScriptService;
import org.ngrinder.script.util.ScriptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Script service implement
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Service
public class ScriptServiceImpl implements ScriptService {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ScriptServiceImpl.class);

	@Autowired
	private ScriptDao scriptDao;

	@Autowired
	private ScriptUtil scriptUtil;

	@Override
	public Page<Script> getScripts(boolean share, String searchStr, Pageable pageable) {
		Page<Script> scripts = scriptDao.getScripts(share, searchStr, pageable);
		return scripts;
	}

	@Override
	public Script getScript(long id) {
		Script script = scriptDao.findOne(id);
		if (null != script) {
			scriptUtil.getContent(script);
			script.setCacheContent(scriptUtil.getScriptCache(id));
			script.setHistoryFileNames(this.getHistoryFileName(id));
		}
		return script;
	}

	@Override
	public Script getScript(long id, String historyName) {
		Script script = this.getScript(id);
		if (null != script) {
			scriptUtil.getHistoryContent(script, historyName);
		}
		return script;
	}

	@Override
	public void saveScript(Script script) {
		if (null != script.getId() && 0 != script.getId().longValue()) {
			Script scriptOld = this.getScript(script.getId());
			if (null != scriptOld) {
				scriptUtil.saveScriptHistoryFile(scriptOld);
			}
		}
		scriptDao.save(script);
		scriptUtil.saveScriptFile(script);
		scriptUtil.deleteScriptCache(script.getId());
	}

	@Override
	public void deleteScript(long id) {
		scriptDao.delete(id);
		scriptUtil.deleteScriptFile(id);
	}

	private List<String> getHistoryFileName(long id) {
		List<String> historyFileNames = scriptUtil.getHistoryFileNames(id);
		return historyFileNames;
	}

	@Override
	public void autoSave(long id, String content) {
		scriptUtil.saveScriptCache(id, content);
	}

}
