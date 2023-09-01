/**
 * Copyright 2023 The University of Edinburgh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package qupath.fx.prefs.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for an locale preference.
 * @since v0.5.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface LocalePref {
	/**
	 * Optional bundle for externalized string
	 * @return
	 */
	String bundle() default "";
	/**
	 * Key for externalized string that gives the text of the preference
	 * @return
	 */
	String value();
	/**
	 * Request that the locales are restricted to only those recognized as available 
	 * (e.g. there is a corresponding ResourceBundle).
	 * @return
	 */
	boolean availableLanguagesOnly() default false;
}