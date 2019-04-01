/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.perftest.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.grinder.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.script.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Performance Test api Controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/perftest/api")
public class PerfTestApiController extends BaseController {

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private TagService tagService;

	private Gson fileEntryGson;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(FileEntry.class, new FileEntry.FileEntrySerializer());
		fileEntryGson = gsonBuilder.create();
	}

	/**
	 * Get the perf test lists.
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @return perftest/list
	 */
	@RestAPI
	@RequestMapping("/list")
	public HttpEntity<String> getAllList(User user,
										 @RequestParam(required = false) String query,
										 @RequestParam(required = false) String tag,
										 @RequestParam(required = false) String queryFilter,
										 @PageableDefault Pageable pageable) {
		Map<String, Object> result = new HashMap<>();
		Pair<Page<PerfTest>, Pageable> pair = getPerfTests(user, query, tag, queryFilter, pageable);
		Page<PerfTest> tests = pair.getFirst();
		pageable = pair.getSecond();
		annotateDateMarker(tests);
		result.put("tag", tag);
		result.put("totalElements", tests.getTotalElements());
		result.put("number", tests.getNumber());
		result.put("size", tests.getSize());
		result.put("queryFilter", queryFilter);
		result.put("query", query);
		result.put("createdUserId", user.getUserId());
		result.put("tests", tests.getContent());
		putPageIntoModelMap(result, pageable);
		return toJsonHttpEntity(result);
	}

	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@RestAPI
	@DeleteMapping
	public String delete(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.delete(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Stop the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test IDs
	 * @return success json if succeeded.
	 */
	@RestAPI
	@PutMapping(params = "action=stop")
	public String stop(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@RestAPI
	@GetMapping("/search_tag")
	public List<String> searchTag(User user, @RequestParam(required = false) String query) {
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return allStrings;
	}

	private Pair<Page<PerfTest>, Pageable> getPerfTests(User user, String query, String tag, String queryFilter, Pageable pageableParam) {
		Pageable pageable = PageRequest.of(pageableParam.getPageNumber(), pageableParam.getPageSize(),
			pageableParam.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageableParam.getSort());
		Page<PerfTest> tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		if (tests.getNumberOfElements() == 0) {
			pageable = PageRequest.of(0, pageableParam.getPageSize(),
				pageableParam.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageableParam.getSort());
			tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageableParam);
		}
		return Pair.of(tests, pageable);
	}

	private void annotateDateMarker(Page<PerfTest> tests) {
		TimeZone userTZ = TimeZone.getTimeZone(getCurrentUser().getTimeZone());
		Calendar userToday = Calendar.getInstance(userTZ);
		Calendar userYesterday = Calendar.getInstance(userTZ);
		userYesterday.add(Calendar.DATE, -1);
		for (PerfTest test : tests) {
			Calendar localedModified = Calendar.getInstance(userTZ);
			localedModified.setTime(DateUtils.convertToUserDate(getCurrentUser().getTimeZone(),
				test.getLastModifiedDate()));
			if (org.apache.commons.lang.time.DateUtils.isSameDay(userToday, localedModified)) {
				test.setDateString("today");
			} else if (org.apache.commons.lang.time.DateUtils.isSameDay(userYesterday, localedModified)) {
				test.setDateString("yesterday");
			} else {
				test.setDateString("earlier");
			}
		}
	}

}
