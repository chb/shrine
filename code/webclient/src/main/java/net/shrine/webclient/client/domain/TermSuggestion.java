package net.shrine.webclient.client.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Mar 29, 2012
 */
public final class TermSuggestion implements IsSerializable {
	private String path;

	private String simpleName;

	private String highlight;

	private String synonym;

	private String category;

	private boolean isLeaf;

	// NB: For GWT
	@SuppressWarnings("unused")
	private TermSuggestion() {
		super();
	}

	public TermSuggestion(final String path, final String simpleName, final String highlight, final String synonym, final String category, final boolean isLeaf) {
		super();

		this.path = path;
		this.simpleName = simpleName;
		this.highlight = highlight;
		this.synonym = synonym;
		this.category = category;
		this.isLeaf = isLeaf;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(final String simpleName) {
		this.simpleName = simpleName;
	}

	public String getHighlight() {
		return highlight;
	}

	public void setHighlight(final String highlight) {
		this.highlight = highlight;
	}

	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(final String synonym) {
		this.synonym = synonym;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(final boolean isLeaf) {
		this.isLeaf = isLeaf;
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
