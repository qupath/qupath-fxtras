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
 * Annotation for a general preference.
 * @since v0.5.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Pref {
	/**
	 * Optional bundle for externalized string
	 * @return
	 */
	String bundle() default "";
	/**
	 * Type for the property
	 * @return
	 */
	Class<?> type();
	/**
	 * Key for externalized string that gives the text of the preference
	 * @return
	 */
	String value();
	/**
	 * Name of a method that can be invoked to get a list of available choices.
	 * This is not needed for Enum types, where the choices are already known.
	 * <p>
	 * The method is expected to be defined in the parent object containing the 
	 * annotated property field. It should take no parameters.
	 * @return
	 */
	String choiceMethod() default "";
	
}