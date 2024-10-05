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
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class JinjaTemplatEngineTest {
	
	JinjaTemplateEngine templateEngine;
	
	
	@BeforeEach
	void setup () {
		var db = Mockito.mock(DB.class);
		var fileSystem = Mockito.mock(DBFileSystem.class);
		var theme = Mockito.mock(Theme.class);
		var parentTheme = Mockito.mock(Theme.class);
		var serverProperties = Mockito.mock(ServerProperties.class);
		
//		Mockito.when(serverProperties.dev()).thenReturn(Boolean.FALSE);
		Mockito.when(db.getFileSystem()).thenReturn(fileSystem);
		Mockito.when(fileSystem.resolve("templates/")).thenReturn(Path.of("src/test/resources/site"));
		Mockito.when(theme.templatesPath()).thenReturn(Path.of("src/test/resources/theme"));
		Mockito.when(theme.getParentTheme()).thenReturn(parentTheme);
		Mockito.when(parentTheme.templatesPath()).thenReturn(Path.of("src/test/resources/theme2"));
		
		templateEngine = new JinjaTemplateEngine(db, serverProperties, theme);
	}
	

	@Test
	public void load_template_from_site() throws IOException {
		String result = templateEngine.render("only-site.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("site");
	}
	
	@Test
	public void load_template_from_theme() throws IOException {
		String result = templateEngine.render("only-theme.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("theme");
	}
	
	@Test
	public void load_template_from_parent_theme() throws IOException {
		String result = templateEngine.render("parent.vm", new TemplateEngine.Model(null, null));
		Assertions.assertThat(result).isEqualTo("from parent theme");
	}
	
	@Test
	public void load_overriden_template() throws IOException {
		String result = templateEngine.render("test.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("from site");
	}
	
	@Test
	public void reload_theme() throws IOException {
		
		var theme = Mockito.mock(Theme.class);
		Mockito.when(theme.templatesPath()).thenReturn(Path.of("src/test/resources/theme2"));
		templateEngine.updateTheme(theme);
		
		
		String result = templateEngine.render("only-theme.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("theme2");
	}
	
	@Test
	public void invalidate_cache() throws IOException {
		String result = templateEngine.render("only-site.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("site");
		
		templateEngine.invalidateCache();
		
		result = templateEngine.render("only-site.vm", new TemplateEngine.Model(null, null));		
		Assertions.assertThat(result).isEqualTo("site");
	}
	
	@Test
	public void string_rendering() throws IOException {
		var model = new TemplateEngine.Model(null, null);
		model.values.put("name", "CondationCMS");
		String result = templateEngine.renderFromString("Hello {{name}}", model);		
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}
}
