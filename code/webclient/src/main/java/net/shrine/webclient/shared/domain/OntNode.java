package net.shrine.webclient.shared.domain;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author clint
 * @date Apr 2, 2012
 */
public final class OntNode {
    //Must be default-access, not private, to appease RestyGWT
    @JsonProperty
    final Term term;

    //Must be default-access, not private, to appease RestyGWT
    @JsonProperty
    final boolean isLeaf;

    @JsonProperty
    private final List<OntNode> children;

    public OntNode(final Term term, final boolean isLeaf) {
        this(term, Collections.<OntNode> emptyList(), isLeaf);
    }

    @JsonCreator
    public OntNode(@JsonProperty("term") final Term term, @JsonProperty("children") final List<OntNode> children, @JsonProperty("isLeaf") final boolean isLeaf) {
        super();

        this.term = term;
        this.children = children;
        this.isLeaf = isLeaf;
    }

    public Term toTerm() {
        return term;
    }

    public String getValue() {
        return term.getPath();
    }

    public List<OntNode> getChildren() {
        return children;
    }

    public String getSimpleName() {
        return term.getSimpleName();
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public String toString() {
        return "OntNode['" + term + "' leaf? " + isLeaf + " " + children + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (children == null ? 0 : children.hashCode());
        result = prime * result + (isLeaf ? 1231 : 1237);
        result = prime * result + (term == null ? 0 : term.hashCode());
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
        final OntNode other = (OntNode) obj;
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (isLeaf != other.isLeaf) {
            return false;
        }
        if (term == null) {
            if (other.term != null) {
                return false;
            }
        } else if (!term.equals(other.term)) {
            return false;
        }
        return true;
    }
}
