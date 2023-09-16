package red.jackf.chesttracker.gui.util;

import com.blamejared.searchables.api.SearchableType;
import com.blamejared.searchables.api.TokenRange;
import com.blamejared.searchables.api.formatter.FormattingContext;
import com.blamejared.searchables.lang.StringSearcher;
import com.blamejared.searchables.lang.expression.type.ComponentExpression;
import com.blamejared.searchables.lang.expression.type.GroupingExpression;
import com.blamejared.searchables.lang.expression.type.LiteralExpression;
import com.blamejared.searchables.lang.expression.type.PairedExpression;
import com.blamejared.searchables.lang.expression.visitor.ContextAwareVisitor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CustomSearchablesFormatter implements ContextAwareVisitor<TokenRange, FormattingContext>, Consumer<String>, BiFunction<String, Integer, FormattedCharSequence> {

    private final SearchableType<?> type;

    private final List<Pair<TokenRange, Style>> tokens = new ArrayList<>();
    private TokenRange lastRange = TokenRange.at(0);

    public CustomSearchablesFormatter(final SearchableType<?> type) {
        this.type = type;
    }

    /**
     * Resets this visitor to a state that allows it to run again.
     */
    public void reset() {

        tokens.clear();
        lastRange = TokenRange.at(0);
    }

    @NotNull
    public TokenRange visitGrouping(final GroupingExpression expr, final @NotNull FormattingContext context) {

        TokenRange leftRange = expr.left().accept(this, context);
        tokens.add(Pair.of(getAndPushRange(), context.style()));
        TokenRange rightRange = expr.right().accept(this, context);
        return TokenRange.encompassing(leftRange, rightRange);
    }

    @NotNull
    public TokenRange visitComponent(final ComponentExpression expr, final FormattingContext context) {

        boolean valid = context.valid() && expr.left() instanceof LiteralExpression && expr.right() instanceof LiteralExpression;
        TokenRange leftRange = expr.left()
                .accept(this, FormattingContext.key(Style.EMPTY.withColor(TextColours.getSearchKeyColour()), valid));
        tokens.add(Pair.of(getAndPushRange(), context.style(valid)));
        TokenRange rightRange = expr.right()
                .accept(this, FormattingContext.literal(Style.EMPTY.withColor(TextColours.getSearchTermColour()), valid));
        return TokenRange.encompassing(leftRange, rightRange);
    }

    @Override
    @NotNull
    public TokenRange visitLiteral(final @NotNull LiteralExpression expr, final FormattingContext context) {

        Style style = context.style();
        if (!context.valid() || context.isKey() && !type.components().containsKey(expr.value())) {
            style = Style.EMPTY.withColor(TextColours.getErrorColour()).withUnderlined(true);
        }
        TokenRange range = getAndPushRange(expr.displayValue().length());
        tokens.add(Pair.of(range, style));
        return range;
    }

    @Override
    @NotNull
    public TokenRange visitPaired(final PairedExpression expr, final @NotNull FormattingContext context) {

        TokenRange leftRange = expr.first().accept(this, context);
        TokenRange rightRange = expr.second().accept(this, context);
        return TokenRange.encompassing(leftRange, rightRange);
    }

    private TokenRange getAndPushRange() {

        return getAndPushRange(1);
    }

    private TokenRange getAndPushRange(final int end) {

        TokenRange oldRange = lastRange;
        lastRange = TokenRange.between(lastRange.end(), lastRange.end() + end);
        return TokenRange.between(oldRange.end(), oldRange.end() + end);
    }

    @Override
    public void accept(final String search) {

        reset();
        StringSearcher.search(search, this, FormattingContext.empty());
    }

    @Override
    public FormattedCharSequence apply(final String currentString, final Integer offset) {

        List<FormattedCharSequence> sequences = new ArrayList<>();
        int index = 0;
        for (Pair<TokenRange, Style> token : tokens) {
            TokenRange range = token.getFirst();
            int subEnd = Math.max(range.start() - offset, 0);
            if (subEnd >= currentString.length()) {
                break;
            }

            int subStart = Math.min(range.end() - offset, currentString.length());
            if (subStart > 0) {
                sequences.add(FormattedCharSequence.forward(currentString.substring(index, subEnd), token.getSecond()));
                sequences.add(FormattedCharSequence.forward(currentString.substring(subEnd, subStart), token.getSecond()));
                index = subStart;
            }
        }
        sequences.add(FormattedCharSequence.forward(currentString.substring(index), Style.EMPTY));

        return FormattedCharSequence.composite(sequences);
    }
}
