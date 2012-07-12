package org.ngrinder.script.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.script.model.Script;
import org.ngrinder.script.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class ScriptServiceTest extends AbstractNGNinderTransactionalTest {

	private static List<Script> scripts = new ArrayList<Script>();

	private int countNum = 0;

	@Autowired
	private ScriptService scriptService;

	@After
	public void clearScript() {
		for (Script script : scripts) {
			scriptService.deleteScript(script.getId());
		}
	}

	@Test
	public void testSaveScript() {
		this.saveScript("save");
	}

	@Test
	public void testUpdateScript() {
		Script script = this.saveScript("update");
		long id = script.getId();
		script = new Script();
		script.setId(id);
		script.setContent("testUpdateScript");
		script.setCreatedDate(new Date());
		script.setCreatedUser(getUser("user"));
		script.setDescription("test");
		script.setFileName("testUpdateScript.py");
		script.setFileSize(123);
		script.setLastModifiedDate(new Date());
		script.setCreatedUser(getUser("user"));
		script.setShare(false);
		script.setTestURL("www.google.com");

		scriptService.saveScript(script);
		long idNew = script.getId();

		Assert.assertEquals(id, idNew);

		Script scriptNew = scriptService.getScript(script.getId());

		Assert.assertEquals(script, scriptNew);
	}

	@Test
	public void testGetScript() {
		Script script = this.saveScript("get");
		Script scriptNew = scriptService.getScript(script.getId());

		Assert.assertEquals(script, scriptNew);
	}

	@Test
	public void testGetHistoryScript() {
		Script script = this.saveScript("getHistory");
		long id = script.getId();
		script = new Script();
		script.setId(id);
		script.setContent("testNewScript  001");
		script.setCreatedDate(new Date());
		script.setCreatedUser(getUser("user"));
		script.setDescription("test");
		script.setFileName("testScript.py");
		script.setFileSize(123);
		script.setLastModifiedDate(new Date());
		script.setCreatedUser(getUser("user"));
		script.setShare(false);
		script.setTestURL("www.google.com");

		scriptService.saveScript(script);

		List<String> historyFileNames = scriptService.getScript(script.getId()).getHistoryFileNames();
		Assert.assertNotNull(historyFileNames);
		Assert.assertFalse(historyFileNames.isEmpty());
		Script scriptNew = scriptService.getScript(script.getId(), historyFileNames.get(0));
		Assert.assertNotNull(scriptNew.getHistoryContent());
	}

	@Test
	public void testDeleteScript() {
		Page<Script> scriptPage = scriptService.getScripts(true, null, null);

		Script script = this.saveScript("delete");
		scriptService.deleteScript(script.getId());

		Page<Script> scriptPage2 = scriptService.getScripts(true, null, null);
		Assert.assertEquals(scriptPage.getTotalElements(), scriptPage2.getTotalElements());

		Script scriptNew = scriptService.getScript(script.getId());
		Assert.assertNull(scriptNew);

		scripts.remove(script);
	}

	@Test
	public void testGetScripts() {

		this.clearScript();

		// TODO: need to modify.
		// current user is set in Test Based class, if you want to test
		// different script
		// with different user, need to modify this.
		// User user = new User();
		// user.setId(123L);
		// user.setUserName("tmp_user01");
		// UserUtil.setCurrentUser(user);

		Script script = this.saveScript("1");
		script.setFileName("e.py");
		script.setTestURL("v.baidu.com");

		Script script2 = this.saveScript("2");
		script2.setFileName("d.py");
		script2.setTestURL("w.baidu.com");

		Script script3 = this.saveScript("3");
		script3.setFileName("c.py");
		script3.setTestURL("x.baidu.com");

		Script script4 = this.saveScript("4");
		script4.setFileName("b.py");
		script4.setTestURL("y.baidu.com");

		// User user2 = new User();
		// user2.setId(234L);
		// user2.setUserName("tmp_user02");
		// UserUtil.setCurrentUser(user2);

		Script script5 = this.saveScript("5");
		script5.setFileName("a.py");
		script5.setTestURL("z.baidu.com");

		// UserUtil.setCurrentUser(user);

		Order order1 = new Order(Direction.ASC, "fileName");
		Order order2 = new Order(Direction.DESC, "testURL");
		Sort sort = new Sort(order1, order2);
		Pageable pageable = new PageRequest(2, 2, sort);
		Page<Script> scripts = scriptService.getScripts(true, "TEST_USER", pageable);

		Assert.assertNotNull(scripts);
		Assert.assertEquals(2, scripts.getContent().size());
		Assert.assertEquals("c.py", scripts.getContent().get(0).getFileName());
		Assert.assertEquals("d.py", scripts.getContent().get(1).getFileName());
	}

	@Test(timeout = 5000)
	public void testGetScriptsPerformance() {

		// User user = new User();
		// user.setId(987L);
		// user.setUserName("default_tmp_user");
		// UserUtil.setCurrentUser(user);

		this.testGetScriptsPerformance2();

		long startSearch = new Date().getTime();

		Order order1 = new Order(Direction.ASC, "fileName");
		Order order2 = new Order(Direction.DESC, "testURL");
		Sort sort = new Sort(order1, order2);
		Pageable pageable = new PageRequest(5, 15, sort);
		Page<Script> scripts = scriptService.getScripts(true, "TEST_USER", pageable);

		long endSearch = new Date().getTime();
		System.out.println(endSearch - startSearch);

		Assert.assertNotNull(scripts);
		Assert.assertEquals(15, scripts.getContent().size());
	}

	private void testGetScriptsPerformance2() {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		CompletionService<Void> cs = new ExecutorCompletionService<Void>(pool);
		long startInsert = new Date().getTime();
		for (int i = 0; i < 10; i++) {
			cs.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					for (int i = 0; i < 10; i++) {
						saveScript("_" + ++countNum);
					}
					return null;
				}

			});
		}
		for (int i = 0; i < 10; i++) {
			try {
				cs.take().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		long endInsert = new Date().getTime();
		System.out.println(endInsert - startInsert);
	}

	private Script saveScript(String key) {
		if (null == key) {
			key = "";
		}
		Script script = new Script();
		script.setContent("testScript" + key);
		script.setDescription("test" + key);
		script.setFileName("testScript" + key + ".py");
		script.setFileSize(123);
		script.setShare(false);
		script.setTestURL("www.baidu.com" + key);

		Tag tag1 = new Tag();
		tag1.setName("ngrinder");

		Tag tag2 = new Tag();
		tag2.setName("naver");

		Tag tag3 = new Tag();
		tag3.setName("hangame");

		script.addTag(tag1);
		script.addTag(tag2);
		script.addTag(tag3);

		// create
		scriptService.saveScript(script);
		// update
		scriptService.saveScript(script);

		scripts.add(script);
		return script;
	}
}
