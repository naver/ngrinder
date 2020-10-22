package org.ngrinder.compatibility.perftest.controller;

import org.ngrinder.agent.service.AgentService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.controller.PerfTestApiController;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Performance Test api Controller for compatibility with form type.
 *
 * @since 3.5.0
 */
@Profile("production")
@RestController
@RequestMapping(value = "/perftest/api", consumes = APPLICATION_FORM_URLENCODED_VALUE)
@Deprecated
public class PerfTestFormTypeApiController extends PerfTestApiController {

	public PerfTestFormTypeApiController(PerfTestService perfTestService, TagService tagService, AgentManager agentManager,
										 RegionService regionService, AgentService agentService, FileEntryService fileEntryService,
										 UserService userService, HazelcastService hazelcastService, ScriptHandlerFactory scriptHandlerFactory,
										 UserContext userContext, Config config) {
		super(perfTestService, tagService, agentManager, regionService, agentService, fileEntryService,
			userService, hazelcastService,scriptHandlerFactory, userContext, config);
	}

	@PostMapping("/save")
	public PerfTest saveOne(User user, PerfTest perfTest, @RequestParam(defaultValue = "false") boolean isClone) {
		return super.saveOne(user, perfTest, isClone);
	}

	@PostMapping("/{id}/leave_comment")
	public void leaveComment(User user, @PathVariable Long id, @RequestParam Map<String, Object> params) {
		super.leaveComment(user, id ,params);
	}

	@PostMapping("/quickstart")
	public Map<String, Object> getQuickStart(User user, @RequestParam Map<String, Object> params) {
		return super.getQuickStart(user, params);
	}

	@PostMapping({"/", ""})
	public PerfTest create(User user, PerfTest perfTest) {
		return super.create(user, perfTest);
	}

	@PutMapping("/{id}")
	public PerfTest update(User user, @PathVariable Long id, PerfTest perfTest) {
		return super.update(user, id, perfTest);
	}

	@PutMapping(value = "/{id}", params = "action=status")
	public PerfTest updateStatus(User user, @PathVariable Long id, Status status) {
		checkNotNull(status, "Status must present. you can use 'status' for data key");
		return super.updateStatus(user, id, status);
	}

	@PostMapping({"/{id}/clone_and_start", /* for backward compatibility */ "/{id}/cloneAndStart"})
	public PerfTest cloneAndStartPost(User user, @PathVariable Long id, PerfTest perftest) {
		return super.cloneAndStartPost(user, id, perftest);
	}
}
