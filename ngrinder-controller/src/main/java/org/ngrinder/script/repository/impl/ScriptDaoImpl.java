package org.ngrinder.script.repository.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.ngrinder.common.NGrinderConstants;
import org.ngrinder.common.util.ReflectionUtil;
import org.ngrinder.script.model.Script;
import org.ngrinder.script.repository.ScriptDao;
import org.ngrinder.script.repository.ScriptsCache;
import org.ngrinder.script.util.ScriptUtil;
import org.ngrinder.user.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;

/**
 * Zhifei
 * 
 * @date 2012-6-13
 */
@Repository
public class ScriptDaoImpl implements ScriptDao {

	private static final Logger LOG = LoggerFactory.getLogger(ScriptDaoImpl.class);

	// private final Map<Long, Script> scriptsCache = new
	// ConcurrentHashMap<Long, Script>();

	// @PostConstruct
	// public void init() {
	// this.findAll();
	// }

	@Override
	public List<Script> findAll() {
		List<Script> scripts = new ArrayList<Script>();
		File root = new File(NGrinderConstants.PATH_PROJECT);
		File[] userDirs = root.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith(NGrinderConstants.PREFIX_USER);
			}

		});
		if (null != userDirs && userDirs.length > 0) {
			for (File userDir : userDirs) {
				long userId = 0;
				try {
					userId = Long.valueOf(userDir.getName().substring(NGrinderConstants.PREFIX_USER.length()));
				} catch (NumberFormatException e) {
					continue;
				}

				File[] scriptDirs = userDir.listFiles(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return pathname.getName().startsWith(NGrinderConstants.PREFIX_SCRIPT);
					}

				});
				if (null != scriptDirs && scriptDirs.length > 0) {
					for (File scriptDir : scriptDirs) {
						long id = 0;
						try {
							id = Long.valueOf(scriptDir.getName().substring(NGrinderConstants.PREFIX_SCRIPT.length()));
						} catch (NumberFormatException e) {
							continue;
						}
						Script script = this.findOne(userId, id);
						scripts.add(script);
					}
				}

			}
		}
		return scripts;
	}

	@Override
	public Page<Script> getScripts(boolean share, String searchStr, Pageable pageable) {
		Page<Script> page = null;

		List<Script> scripts = new ArrayList<Script>();
		List<Script> scriptCache = ScriptsCache.getInstance().get();

		if (null != pageable) {
			final Sort sort = pageable.getSort();
			this.sortScripts(scriptCache, sort);
		}

		if (null != searchStr) {
			searchStr = searchStr.toLowerCase();
		}

		int i = 0;
		for (Script script : scriptCache) {
			if (!(share && script.isShare()) && !script.getCreateUser().equals(UserUtil.getCurrentUser().getUserName())) {
				continue;
			}

			if (null != searchStr && !script.getFileName().toLowerCase().contains(searchStr)
					&& !script.getTags().toString().toLowerCase().contains(searchStr)
					&& !script.getLastModifiedUser().toLowerCase().contains(searchStr)) {
				continue;
			}

			i++;
			if (null != pageable && (i <= (pageable.getOffset() - pageable.getPageSize()) || i > pageable.getOffset())) {
				continue;
			}

			scripts.add(script);
		}

		page = new PageImpl<Script>(scripts, pageable, scriptCache.size());

		return page;
	}

	private void sortScripts(List<Script> scripts, final Sort sort) {
		if (null == sort) {
			return;
		}
		Comparator<Script> comparator = new Comparator<Script>() {

			@Override
			public int compare(Script s1, Script s2) {
				int result = 0;
				Iterator<Order> iterator = sort.iterator();
				while (iterator.hasNext()) {
					Order order = iterator.next();
					Direction direction = order.getDirection();
					String property = order.getProperty();
					Object o1 = ReflectionUtil.getFieldValue(s1, property);
					Object o2 = ReflectionUtil.getFieldValue(s2, property);

					String c1 = null != o1 ? o1.toString() : "";
					String c2 = null != o2 ? o2.toString() : "";

					result = c1.compareToIgnoreCase(c2);
					if (0 != result) {
						if (direction == Direction.DESC) {
							result = -result;
						}
						break;
					}
				}
				return result;
			}

		};
		Collections.sort(scripts, comparator);
	}

	private Script findOne(long userId, long id) {

		Script script = ScriptsCache.getInstance().get(id);

		if (null == script) {

			String scriptPath = ScriptUtil.getScriptPath(userId, id);
			String scriptPropertiesPath = scriptPath + NGrinderConstants.SCRIPT_PROPERTIES;

			FileInputStream fis = null;
			ObjectInputStream ois = null;
			try {
				fis = new FileInputStream(new File(scriptPropertiesPath));
				ois = new ObjectInputStream(fis);
				script = (Script) ois.readObject();
				ScriptsCache.getInstance().put(script);
			} catch (IOException e) {
				LOG.error("Deserialize Script bean failed.", e);
			} catch (ClassNotFoundException e) {
				LOG.error("Cast the class to Script failed.", e);
			} finally {
				IOUtils.closeQuietly(fis);
				IOUtils.closeQuietly(ois);
			}
		}

		return script;

	}

	@Override
	public Script findOne(long id) {
		return this.findOne(0, id);
	}

	@Override
	public void save(Script script) {
		if (null == script.getId() || 0 == script.getId().longValue()) {
			script.setId((long) script.hashCode());
			ScriptUtil.createScriptPath(script.getId());
		}
		String scriptPath = ScriptUtil.getScriptPath(script.getId());

		String scriptPropertiesPath = scriptPath + NGrinderConstants.SCRIPT_PROPERTIES;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(scriptPropertiesPath);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(script);
			ScriptsCache.getInstance().put(script);
		} catch (IOException e) {
			LOG.error("Serialize Script bean failed.", e);
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(oos);
		}
	}

	@Override
	public void delete(long id) {
		ScriptsCache.getInstance().remove(id);
	}

}
