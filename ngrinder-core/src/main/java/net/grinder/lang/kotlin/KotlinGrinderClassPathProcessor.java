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
package net.grinder.lang.kotlin;

import net.grinder.util.AbstractGrinderClassPathProcessor;

import java.util.List;

public class KotlinGrinderClassPathProcessor extends AbstractGrinderClassPathProcessor {

	public KotlinGrinderClassPathProcessor() {
		super();
	}

	@Override
	protected void initMore() {
		List<String> usefulJarList = getUsefulJarList();
		usefulJarList.add("ngrinder-kotlin");
		usefulJarList.add("trove4j");
		usefulJarList.add("kotlin-compiler-embeddable");
		usefulJarList.add("kotlin-reflect");
		usefulJarList.add("kotlin-script-runtime");
		usefulJarList.add("kotlin-script-util");
		usefulJarList.add("kotlin-scripting-common");
		usefulJarList.add("kotlin-scripting-compiler-embeddable");
		usefulJarList.add("kotlin-scripting-compiler-impl-embeddable");
		usefulJarList.add("kotlin-scripting-jvm");
		usefulJarList.add("kotlin-stdlib");
	}

}
