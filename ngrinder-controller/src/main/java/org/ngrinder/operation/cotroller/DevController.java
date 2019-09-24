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
package org.ngrinder.operation.cotroller;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

/**
 * Developer Feature.
 *
 * @since 3.3
 */
@Controller
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {

	private final ReloadableResourceBundleMessageSource messageSource;

	/**
	 * Refresh the messages.
	 *
	 * @param model model
	 * @return "redirect:/"
	 */
	@GetMapping("/msg")
	public String refreshMessage(ModelMap model) {
		messageSource.clearCacheIncludingAncestors();
		model.clear();
		return "redirect:/";
	}

}
