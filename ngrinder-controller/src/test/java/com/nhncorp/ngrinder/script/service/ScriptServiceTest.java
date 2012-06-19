package com.nhncorp.ngrinder.script.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.nhncorp.ngrinder.NGrinderIocTransactionalTestBase;
import com.nhncorp.ngrinder.script.model.Script;
import com.nhncorp.ngrinder.script.model.Tag;

public class ScriptServiceTest extends NGrinderIocTransactionalTestBase {

	private static List<Script> scripts = new ArrayList<Script>();

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
		this.saveScript();
	}

	@Test
	public void testUpdateScript() {
		Script script = this.saveScript();
		long id = script.getId();
		script = new Script();
		script.setId(id);
		script.setContent("testUpdateScript");
		script.setCreateDate(new Date());
		script.setCreateUser("zhangsan");
		script.setDescription("test");
		script.setFileName("testUpdateScript.py");
		script.setFileSize(123);
		script.setLastModifiedDate(new Date());
		script.setLastModifiedUser("lisi");
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
		Script script = this.saveScript();
		Script scriptNew = scriptService.getScript(script.getId());

		Assert.assertEquals(script, scriptNew);
	}

	@Test
	public void testGetHistoryScript() {
		Script script = this.saveScript();
		long id = script.getId();
		script = new Script();
		script.setId(id);
		script.setContent("testNewScript  001");
		script.setCreateDate(new Date());
		script.setCreateUser("zhangsan");
		script.setDescription("test");
		script.setFileName("testScript.py");
		script.setFileSize(123);
		script.setLastModifiedDate(new Date());
		script.setLastModifiedUser("lisi");
		script.setShare(false);
		script.setTestURL("www.google.com");

		scriptService.saveScript(script);

		List<String> historyFileNames = scriptService.getHistoryFileName(script.getId());
		Assert.assertNotNull(historyFileNames);
		Assert.assertFalse(historyFileNames.isEmpty());
		Script scriptNew = scriptService.getScript(script.getId(), historyFileNames.get(0));
		Assert.assertNotNull(scriptNew.getHistoryContent());
	}

	@Test
	public void testDeleteScript() {
		Script script = this.saveScript();
		scriptService.deleteScript(script.getId());

		Script scriptNew = scriptService.getScript(script.getId());

		Assert.assertNull(scriptNew);
		scripts.remove(script);
	}

	@Test
	public void testGetScripts() {
		this.clearScript();

		Script script = this.saveScript();
		script.setFileName("e.py");
		script.setTestURL("v.baidu.com");
		script.setLastModifiedUser("wangwu");

		Script script2 = this.saveScript();
		script2.setFileName("d.py");
		script2.setTestURL("w.baidu.com");
		script2.setLastModifiedUser("wangwu");

		Script script3 = this.saveScript();
		script3.setFileName("c.py");
		script3.setTestURL("x.baidu.com");
		script3.setLastModifiedUser("wangwu");

		Script script4 = this.saveScript();
		script4.setFileName("b.py");
		script4.setTestURL("y.baidu.com");
		script4.setLastModifiedUser("wangwu");

		Script script5 = this.saveScript();
		script5.setFileName("a.py");
		script5.setTestURL("z.baidu.com");

		Order order1 = new Order(Direction.ASC, "fileName");
		Order order2 = new Order(Direction.DESC, "testURL");
		Sort sort = new Sort(order1, order2);
		Pageable pageable = new PageRequest(2, 2, sort);
		List<Script> scripts = scriptService.getScripts("wangwu", pageable);

		Assert.assertNotNull(scripts);
		Assert.assertEquals(2, scripts.size());
		Assert.assertEquals(scripts.get(0).getFileName(), "d.py");
		Assert.assertEquals(scripts.get(1).getFileName(), "e.py");
	}

	@Test
	public void testGetScriptsPerformance() {
		this.testGetScriptsPerformance2();

		long startSearch = new Date().getTime();

		Order order1 = new Order(Direction.ASC, "fileName");
		Order order2 = new Order(Direction.DESC, "testURL");
		Sort sort = new Sort(order1, order2);
		Pageable pageable = new PageRequest(5, 15, sort);
		List<Script> scripts = scriptService.getScripts("lisi", pageable);

		long endSearch = new Date().getTime();
		System.out.println(endSearch - startSearch);

		Assert.assertNotNull(scripts);
		Assert.assertEquals(15, scripts.size());
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
						saveScript();
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

	private Script saveScript() {
		Script script = new Script();
		script.setContent("testScript");
		script.setCreateDate(new Date());
		script.setCreateUser("zhangsan");
		script.setDescription("test");
		script.setFileName("testScript.py");
		script.setFileSize(123);
		script.setLastModifiedDate(new Date());
		script.setLastModifiedUser("lisi");
		script.setShare(false);
		script.setTestURL("www.baidu.com");

		Tag tag1 = new Tag();
		tag1.setName("ngrinder");

		Tag tag2 = new Tag();
		tag2.setName("naver");

		Tag tag3 = new Tag();
		tag3.setName("hangame");

		script.addTag(tag1);
		script.addTag(tag2);
		script.addTag(tag3);

		scriptService.saveScript(script);

		scripts.add(script);
		return script;
	}
}
