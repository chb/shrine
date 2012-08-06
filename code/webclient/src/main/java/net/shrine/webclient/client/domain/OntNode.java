package net.shrine.webclient.client.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 2, 2012
 */
public final class OntNode implements IsSerializable {
	//NB: Field must be public for RestyGWT to serialize it
	public Term term;

	//NB: Field must be public for RestyGWT to serialize it
	public boolean isLeaf;

	//NB: Field must be public for RestyGWT to serialize it
	public List<OntNode> children;

	// For RestyGWT
	@SuppressWarnings("unused")
	public OntNode() {
		this(null, new ArrayList<OntNode>(), false);
	}

	public OntNode(final Term term, final boolean isLeaf) {
		this(term, Collections.<OntNode>emptyList(), isLeaf);
	}
	
	public OntNode(final Term term, final List<OntNode> children, final boolean isLeaf) {
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

	public void setChildren(final List<OntNode> children) {
		this.children = children;
	}

	public String getSimpleName() {
		return term.getSimpleName();
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(final boolean isLeaf) {
		this.isLeaf = isLeaf;
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
