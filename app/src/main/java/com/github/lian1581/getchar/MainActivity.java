package com.github.lian1581.getchar;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity 
{
    /** 选中文字 */
    private String selectedText;

    /** 选中内容为只读 */
    private boolean onlyRead;
    
    /** 返回的文字结果 */
    private CharSequence ret="";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        selectedText = getIntent().getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        onlyRead = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);

        try
        {
            ret = str2oth(selectedText);        
        }
        catch (UnsupportedEncodingException e)
        {
            Toast.makeText(this, getString(R.string.Unsupported_encoding), Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!onlyRead)
        {
            // 替换选中内容
            Intent intent =new Intent();
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, ret);
            setResult(RESULT_OK, intent);
        }
        else
        {
            ClipboardManager clipboardManager= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData primaryClip = ClipData.newPlainText("Label", ret);//纯文本内容
            clipboardManager.setPrimaryClip(primaryClip);
            Toast.makeText(this, getString(R.string.Unable_to_edit), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private CharSequence str2oth(String text) throws UnsupportedEncodingException
    {
        String s;
        // unicode转16进制
        if (text.contains("\\u") & !text.contains("[g-zG-Z9[^uU]]"))
        {
            s = uni2Str(text);
            return str2Hex(s);
        }

        // 16进制转8进制
        if (text.contains("\\x") & !text.contains("[g-zG-Z9[^xX]]"))
        {
            s = hex2Str(text);
            return str2Oct(s);
        }

        // 8进制转字符
        if (text.contains("\\") & !text.contains("[A-Za-z89]"))
        {
            return oct2Str(text);
        }
        // 字符转unicode
        return str2Uni(text);
    }

    /*
     * 字符串转8进制编码
     */
    public String str2Oct(String text)
    {
        String result = "";
        byte[] bytes = text.getBytes();
        for (byte b : bytes)
        {
            int b1 = b;
            if (b1 < 0) b1 = 256 + b1;
            result += "\\" + (b1 / 64) % 8 +  "" + (b1 / 8) % 8 + "" + b1 % 8;
        }
        return result;
    }

    /*
     * 8进制编码转字符串
     */
    public String oct2Str(String s) throws UnsupportedEncodingException
    {
        String[] as = s.split("\\\\");
        byte[] arr = new byte[as.length - 1];
        for (int i = 1; i < as.length; i++)
        {
            int sum = 0;
            int base = 64;
            for (char c : as[i].toCharArray())
            {
                sum += base * ((int)c - '0');
                base /= 8;
            }
            if (sum >= 128) sum = sum - 256;
            arr[i - 1] = (byte)sum;
        }
        return new String(arr, "UTF-8");
    }

    /*
     * 字符串转Unicode编码
     */
    public String str2Uni(String s)
    {
        String str = "";
        for (int i = 0; i < s.length(); i++)
        {
            int ch = s.charAt(i);
            str += "\\u" + Integer.toHexString(ch);
        }
        return str;
    }

    /*
     * Unicode编码转字符串
     */
    public String uni2Str(String unicode)
    {
        String[] strs = unicode.split("\\\\u");
        String returnStr = "";
        // 由于unicode字符串以 \ u开头，因此分割出的第一个字符是""。
        for (int i = 1; i < strs.length; i++)
        {
            returnStr += (char) Integer.valueOf(strs[i], 16).intValue();
        }
        return returnStr;
    }

    /**
     * 字符串转16进制编码
     */
    public String str2Hex(String str)
    {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append("\\x").append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     *  16进制编码转字符串
     */
    public String hex2Str(String str) throws UnsupportedEncodingException
    {
        String strArr[] = str.split("\\\\"); // 分割拿到形如 xE9 的16进制数据
        byte[] byteArr = new byte[strArr.length - 1];
        for (int i = 1; i < strArr.length; i++)
        {
            Integer hexInt = Integer.decode("0" + strArr[i]);
            byteArr[i - 1] = hexInt.byteValue();
        }
        return new String(byteArr, "UTF-8");
    }
}
