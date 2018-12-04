package com.roy.football.match.OFN.out.header;

public class HeaderConfiguration {
	
	public HeaderConfiguration(){};

	public HeaderConfiguration(String title, Integer order, Boolean writale,
			Class<?> dataType) {
		this.title = title;
		this.order = order;
		this.writale = writale;
		this.dataType = dataType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result
				+ ((dataType == null) ? 0 : dataType.getName().hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((writale == null) ? 0 : writale.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeaderConfiguration other = (HeaderConfiguration) obj;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (propertyName != other.propertyName)
			return false;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (dataType != other.dataType)
			return false;
		if (order == null) {
			if (other.order != null)
				return false;
		} else if (!order.equals(other.order))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (writale == null) {
			if (other.writale != null)
				return false;
		} else if (!writale.equals(other.writale))
			return false;
		return true;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public Boolean getWritale() {
		return writale;
	}
	public void setWritale(Boolean writale) {
		this.writale = writale;
	}
	public Class<?> getDataType() {
		return dataType;
	}
	public void setDataType(Class<?> dataType) {
		this.dataType = dataType;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	private String propertyName;
	private String title;
	private Integer order;
	private Boolean writale;
	private Class<?> dataType;
}
