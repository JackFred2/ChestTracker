package red.jackf.chesttracker.gui;

import com.blamejared.searchables.api.SearchableType;
import com.blamejared.searchables.api.formatter.FormattingVisitor;

public class CustomSearchablesFormatter extends FormattingVisitor {
    public CustomSearchablesFormatter(SearchableType<?> type) {
        super(type);
    }
}
