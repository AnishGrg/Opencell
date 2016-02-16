package org.meveo.model.admin;

public enum ModuleItemTypeEnum {
	CET(1, "meveoModuleItemType.CET"), CFT(2, "meveoModuleItemType.CFT"), FILTER(3, "meveoModuleItemType.fliter"), SCRIPT(
			4, "meveoModuleItemType.script"), JOBINSTANCE(5, "meveoModuleItemType.jobInstance"), NOTIFICATION(6,
			"meveoModuleItemType.notification"), SUBMODULE(7, "meveoModuleItemType.subModule"), MEASURABLEQUANTITIES(8,
			"meveoModuleItemType.measurableQuantities"), CHART(9, "meveoModuleItemType.chart");

	private Integer id;
	private String label;

	ModuleItemTypeEnum(Integer id, String label) {
		this.id = id;
		this.label = label;
	}

	public static ModuleItemTypeEnum getValue(Integer id) {
		if (id != null) {
			for (ModuleItemTypeEnum type : values()) {
				if (id.equals(type.getId())) {
					return type;
				}
			}
		}
		return null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
