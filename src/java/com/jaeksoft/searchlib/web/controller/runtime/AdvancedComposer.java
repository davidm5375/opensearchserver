/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;

import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.ocr.TesseractLanguageEnum;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class AdvancedComposer extends CommonController {

	public AdvancedComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public OcrManager getOcrManager() throws SearchLibException, IOException {
		return ClientCatalog.getOcrManager();
	}

	@Command
	public void onCheck() throws SearchLibException, InterruptedException {
		ClientCatalog.getOcrManager().check();
		new AlertController("OK");
	}

	public PropertyItem<Integer> getMaxClauseCount() {
		return ClientFactory.INSTANCE.getBooleanQueryMaxClauseCount();
	}

	public TesseractLanguageEnum[] getTesseractLanguageEnum() {
		return TesseractLanguageEnum.values();
	}

}
