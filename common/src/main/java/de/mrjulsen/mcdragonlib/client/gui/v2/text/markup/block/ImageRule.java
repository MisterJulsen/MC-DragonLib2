/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.BlockLayoutContext;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.BlockLayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.render.IBlockDecorator;

public final class ImageRule implements IBlockRule, IBlockLayout, IBlockDecorator {

    public static final BlockKind KIND = BlockKind.register("image", BlockKind.TRAIT_NONE);

    public static final ImageRule DEFAULT = new ImageRule();

    @Override
    public ParsedLine parse(BlockContext context) {
        int start = context.contentStart();
        if (!MarkupText.startsWith(context.line(), start, "![")) {
            return null;
        }
        int length = context.length();
        int closeBracket = MarkupText.indexOfUnescaped(context.line(), start + 2, length, ']');
        if (closeBracket < 0 || closeBracket + 1 >= length || context.charAt(closeBracket + 1) != '(' || context.charAt(length - 1) != ')') {
            return null;
        }

        context.emitMarkup(0, length);
        String target = context.textBetween(closeBracket + 2, length - 1);
        return context.build(KIND, 0, false, target, ImageRef.parse(target));
    }

    @Override
    public IBlockLayout layoutOf(BlockKind kind) {
        return kind == KIND ? this : null;
    }

    @Override
    public BlockLayoutResult layout(BlockLayoutContext context) {
        ImageRef image = context.parsed().dataAs(ImageRef.class);
        if (context.showsMarkup() || image == null) {
            return BlockLayoutResult.of(context.textRows());
        }
        return BlockLayoutResult.of(new VisualRow(0, context.text().length(), context.indent(),
                image.width(), image.height(), image.height(), List.of(), null), image);
    }

    @Override
    public IBlockDecorator decoratorOf(BlockKind kind) {
        return kind == KIND ? this : null;
    }

    @Override
    public void render(BlockDecoration d) {
        ImageRef image = d.layout().attachmentAs(ImageRef.class);
        if (image == null) {
            return;
        }
        d.graphics().graphics().blit(image.texture(), d.contentX(), d.top(), 0, 0,
                image.width(), image.height(), image.width(), image.height());
    }
}
