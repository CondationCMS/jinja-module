package com.condation.cms.modules.jinja;

/*-
 * #%L
 * jinja-module
 * %%
 * Copyright (C) 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.CascadingResourceLocator;
import com.hubspot.jinjava.loader.FileLocator;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
public class JinjaTemplateEngine implements TemplateEngine {

	private Jinjava templateEngine;
	private Jinjava stringTemplateEngine;

	final DB db;
	final ServerProperties properties;

	public JinjaTemplateEngine(final DB db, final ServerProperties properties, final Theme theme) {

		this.db = db;
		this.properties = properties;
		buildEngine(theme);
	}

	private ResourceLocator createLoader(final DBFileSystem fileSystem, final Theme theme) throws FileNotFoundException {
		List<ResourceLocator> loaders = new ArrayList<>();

		var siteLoader = new FileLocator(fileSystem.resolve("templates/").toFile());
		loaders.add(siteLoader);

		if (!theme.empty()) {
			var themeLoader = new FileLocator(theme.templatesPath().toFile());
			loaders.add(themeLoader);

			if (theme.getParentTheme() != null) {
				var parentLoader = new FileLocator(theme.getParentTheme().templatesPath().toFile());
				loaders.add(parentLoader);
			}
		}

		return new CascadingResourceLocator(loaders.toArray(FileLocator[]::new));
	}

	@Override
	public String render(String template, Model model) throws IOException {
		var templateContent = templateEngine.getResourceLocator().getString(template, StandardCharsets.UTF_8, null);
		return renderFromString(templateContent, model);
	}

	@Override
	public String renderFromString(String templateString, Model model) throws IOException {
		return templateEngine.render(templateString, model.values);
	}

	@Override
	public void invalidateCache() {
//		fileTemplateEngine.getTemplateCache().invalidateAll();
	}

	@Override
	public void updateTheme(Theme theme) {
		buildEngine(theme);
	}

	private void buildEngine(Theme theme) {
		try {
			final JinjavaConfig config = new JinjavaConfig();

			if (properties.dev()) {

			} else {

			}

			templateEngine =  new Jinjava(config);
			templateEngine.setResourceLocator(createLoader(db.getFileSystem(), theme));
		} catch (FileNotFoundException ex) {
			log.error("", ex);
		}
	}

}
