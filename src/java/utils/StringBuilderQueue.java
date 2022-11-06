package utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class StringBuilderQueue {
    private static StringBuilderQueue self;
    private StringBuilder sb;
    private List<Integer> sbIndex;

    private Integer length = 10;

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    private StringBuilderQueue(){
        if(sb == null){
            sb = new StringBuilder();
        }
        if(sbIndex == null){
            sbIndex = new ArrayList<>();
        }
    }
    public static StringBuilderQueue getInstance(){
        if(self == null){
            self = new StringBuilderQueue();
        }
        return self;
    }

    public StringBuilderQueue back(){
        if(sb.length() > 0){
            sbIndex.remove(sb.length()-1);
            sb.setLength(sb.length()-1);
            System.out.println(sb);
        }
        return this;
    }

    public StringBuilderQueue append(char c,Integer index){

        if((index == 10 || c == '~') && sb.length()>0 && sb.charAt(sb.length()-1) == '~' ) {
            //保留原样，啥也不做
        }else if(sb.length()>0 && sb.charAt(sb.length()-1) == '~'){
            sb.setCharAt(sb.length()-1,c);
            sbIndex.set(sb.length()-1,index);
        }else{
            if(sb.length() < length){
                sb.append(c);
                sbIndex.add(index);
            }else{
                String substring = sb.append(c).substring(1);
                sb .setLength(0);
                sb.append(substring);
                int flag = 1;
                for (Integer naWoZou : sbIndex) {
                    if(flag < sb.length()){
                        sbIndex.set(flag-1,sbIndex.get(flag));
                    }else{
                        sbIndex.set(flag-1,index);
                    }
                    flag++;
                }
            }
        }
        if(index == 10 || c == '~'){
            System.out.println(sb);
        }

        return this;
    }

    public String getLastIndexOfString(Integer num){

        if (num >= sb.length()){
            return sb.toString();
        }else{
            List<Integer> other = new ArrayList<>();
            for(int i=sb.length()-num;i<sb.length();i++){
                other.add(sbIndex.get(i));
            }
            return sb.substring(sb.length()-num);
        }
    }


}
