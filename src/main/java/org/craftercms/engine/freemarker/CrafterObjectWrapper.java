/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.freemarker;

import freemarker.ext.util.ModelFactory;
import freemarker.template.DefaultObjectWrapper;
import org.dom4j.Node;
import org.craftercms.engine.model.Dom4jNodeModel;

/**
 * Extends {@link freemarker.template.DefaultObjectWrapper} to define {@link freemarker.ext.util.ModelFactory}s for the following classes:
 *
 * <ul>
 *     <li>{@link org.dom4j.Node}</li>
 * </ul>
 *
 * @author Alfonso Vásquez
 */
@SuppressWarnings("deprecation")
public class CrafterObjectWrapper extends DefaultObjectWrapper {

    @Override
    protected ModelFactory getModelFactory(Class clazz) {
        if (Node.class.isAssignableFrom(clazz)){
            return Dom4jNodeModel.FACTORY;
        }

        return super.getModelFactory(clazz);
    }

}
