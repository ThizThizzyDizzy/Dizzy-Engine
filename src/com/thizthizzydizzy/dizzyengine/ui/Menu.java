package com.thizthizzydizzy.dizzyengine.ui;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
import com.thizthizzydizzy.dizzyengine.ui.component.Panel;
public class Menu extends Panel{
    public Menu(){
        background = null;
    }
    public void open(){
        open(DizzyEngine.getUIContext());
    }
    public void open(UILayer ui){
        ui.open(this);
    }
    public void onMenuOpened(){
    }
    public void onMenuClosed(){
    }
    protected void onCursorFound(int id){
    }
    protected void onCursorLost(int id){
        if(isCursorFocused[id]){
            isCursorFocused[id] = false;
            onCursorFocusLost(id);
        }
    }
}
