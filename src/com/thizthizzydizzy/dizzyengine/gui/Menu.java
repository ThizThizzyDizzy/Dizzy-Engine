package com.thizthizzydizzy.dizzyengine.gui;
import com.thizthizzydizzy.dizzyengine.DizzyEngine;
public class Menu extends Component{
    public void open(){
        DizzyEngine.getLayer(FlatGUI.class).open(this);
    }
    public void onMenuOpened(){}
    public void onMenuClosed(){}
    protected void onCursorFound(int id){}
    protected void onCursorLost(int id){
        if(isCursorFocused[id]){
            isCursorFocused[id] = false;
            onCursorFocusLost(id);
        }
    }
}