package com.codeseven.pos.model;

public class NavMenuItem {
    public String menuName, category_id, imageurl;
    public boolean hasChildren, isGroup;

    public NavMenuItem(String menuName, String category_id, boolean hasChildren, boolean isGroup, String imageurl) {
        this.menuName = menuName;
        this.category_id = category_id;
        this.hasChildren = hasChildren;
        this.isGroup = isGroup;
        this.imageurl = imageurl;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
