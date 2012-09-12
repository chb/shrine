package net.shrine.webclient.shared.domain;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author clint
 * @date Mar 29, 2012
 */
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY/*, value = JsonMethod.FIELD*/)
public final class TermSuggestion {
    @JsonProperty
    private final String path;

    @JsonProperty
    private final String simpleName;

    @JsonProperty
    private final String highlight;

    @JsonProperty
    private final String synonym;

    @JsonProperty
    private final String category;

    //NB: Must be default access, not private, to appease RestyGWT
    @JsonProperty
    final boolean isLeaf;

    @JsonCreator
    public TermSuggestion(@JsonProperty("path") final String path, 
                           @JsonProperty("simpleName") final String simpleName, 
                           @JsonProperty("highlight") final String highlight, 
                           @JsonProperty("synonym") final String synonym, 
                           @JsonProperty("category") final String category, 
                           @JsonProperty("isLeaf") final boolean isLeaf) {
        super();

        this.path = path;
        this.simpleName = simpleName;
        this.highlight = highlight;
        this.synonym = synonym;
        this.category = category;
        this.isLeaf = isLeaf;
    }

    public Term toTerm() {
        return new Term(path, category, simpleName);
    }

    public String getPath() {
        return path;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getHighlight() {
        return highlight;
    }

    public String getSynonym() {
        return synonym;
    }

    public String getCategory() {
        return category;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (category == null ? 0 : category.hashCode());
        result = prime * result + (highlight == null ? 0 : highlight.hashCode());
        result = prime * result + (isLeaf ? 1231 : 1237);
        result = prime * result + (path == null ? 0 : path.hashCode());
        result = prime * result + (simpleName == null ? 0 : simpleName.hashCode());
        result = prime * result + (synonym == null ? 0 : synonym.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TermSuggestion other = (TermSuggestion) obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        } else if (!category.equals(other.category)) {
            return false;
        }
        if (highlight == null) {
            if (other.highlight != null) {
                return false;
            }
        } else if (!highlight.equals(other.highlight)) {
            return false;
        }
        if (isLeaf != other.isLeaf) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (simpleName == null) {
            if (other.simpleName != null) {
                return false;
            }
        } else if (!simpleName.equals(other.simpleName)) {
            return false;
        }
        if (synonym == null) {
            if (other.synonym != null) {
                return false;
            }
        } else if (!synonym.equals(other.synonym)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TermSuggestion [path=" + path + "]";
    }
}
