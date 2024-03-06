package com.thizthizzydizzy.dizzyengine.ui.component;
public class OptionButton extends Button{
    private int index;
    public OptionButton(String text, int startingIndex, String... values){
        super(text+": "+values[startingIndex]);
        index = startingIndex;
        addAction(() -> {
            int idx = index+1;
            if(idx>=values.length)idx = 0;
            index = idx;
            label.setLabel(text+": "+values[index]);
        });
    }
    public int getIndex(){
        return index;
    }
}
