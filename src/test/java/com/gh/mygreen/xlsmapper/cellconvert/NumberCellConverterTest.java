package com.gh.mygreen.xlsmapper.cellconvert;

import static com.gh.mygreen.xlsmapper.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.IsEmptyBuilder;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.annotation.OverRecordOperate;
import com.gh.mygreen.xlsmapper.annotation.RecordTerminal;
import com.gh.mygreen.xlsmapper.annotation.XlsColumn;
import com.gh.mygreen.xlsmapper.annotation.XlsConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsHint;
import com.gh.mygreen.xlsmapper.annotation.XlsHorizontalRecords;
import com.gh.mygreen.xlsmapper.annotation.XlsIsEmpty;
import com.gh.mygreen.xlsmapper.annotation.XlsNumberConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;

/**
 * 数値型の変換のテスタ
 * 
 * @since 0.5
 * @author T.TSUCHIE
 *
 */
public class NumberCellConverterTest {
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * 数値型の読み込みテスト
     */
    @Test
    public void test_load_number() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/convert.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(NumberSheet.class);
            NumberSheet sheet = mapper.load(in, NumberSheet.class, errors);
            
            if(sheet.primitiveRecords != null) {
                for(PrimitiveRecord record : sheet.primitiveRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.wrapperRecords != null) {
                for(WrapperRecord record : sheet.wrapperRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.otherRecords != null) {
                for(OtherRecord record : sheet.otherRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.formattedRecords != null) {
                for(FormattedRecord record : sheet.formattedRecords) {
                    assertRecord(record, errors);
                }
            }
            
        }
        
    }
    
    /**
     * プリミティブ型の値のチェック
     * @param record
     * @param errors
     */
    private void assertRecord(final PrimitiveRecord record, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), record.getClass().getSimpleName(), record.no, record.comment);
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)0));
            assertThat(record.l, is((long)0L));
            assertThat(record.f, is((float)0.0));
            assertThat(record.d, is((double)0.0));
            
        } else if(record.no == 2) {
            // ゼロ（標準型）
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)0));
            assertThat(record.l, is((long)0L));
            assertThat(record.f, is((float)0.0));
            assertThat(record.d, is((double)0.0));
            
        } else if(record.no == 3) {
            // ゼロ（数値型）
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)0));
            assertThat(record.l, is((long)0L));
            assertThat(record.f, is((float)0.0));
            assertThat(record.d, is((double)0.0));
            
        } else if(record.no == 4) {
            // 正の数（標準）
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)12L));
            assertThat(record.f, is((float)12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 5) {
            // 正の数（数値型）
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)12L));
            assertThat(record.f, is((float)12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 6) {
            // 負の数（標準）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)-12.3));
            
        } else if(record.no == 7) {
            // 負の数（数値型）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)-12.3));
            
        } else if(record.no == 8) {
            // 数値（△ 1234）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 9) {
            // 通貨
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)1234567L));
            assertThat(record.f, is((float)12345.67));
            assertThat(record.d, is((double)-12345.67));
            
        } else if(record.no == 10) {
            // 会計
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)12346L));
            assertThat(record.f, is((float)12345.67));
            assertThat(record.d, is((double)-12345.67));
            
        } else if(record.no == 11) {
            // パーセント
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)1));
            assertThat(record.l, is((long)-12));
            assertThat(record.f, is((float)0.1234));
            assertThat(record.d, is((double)-1.234567));
            
        } else if(record.no == 12) {
            // 分数
            assertThat(record.b, is((byte)1));
            assertThat(record.s, is((short)1));
            assertThat(record.i, is((int)-1));
            assertThat(record.l, is((long)123));
            assertThat(record.f, is((float)0.1234));
            assertThat(record.d, is((double)-1.234567));
            
        } else if(record.no == 13) {
            // 指数
            assertThat(record.b, is((byte)1));
            assertThat(record.s, is((short)1));
            assertThat(record.i, is((int)-1));
            assertThat(record.l, is((long)0));
            assertThat(record.f, is((float)0.0001));
            assertThat(record.d, is((double)1000.234));
            
        } else if(record.no == 14) {
            // 指数
            assertThat(record.b, is((byte)123));
            assertThat(record.s, is((short)123));
            assertThat(record.i, is((int)-123));
            assertThat(record.l, is((long)1234567));
            assertThat(record.f, is((float)123.456));
            assertThat(record.d, is((double)-123.456));
            
        } else if(record.no == 15) {
            // 文字列型
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)12));
            assertThat(record.f, is((float)12.34));
            assertThat(record.d, is((double)-12.34));
            
        } else if(record.no == 16) {
            // 最大値（文字列型）
            assertThat(record.b, is((byte)127));
            assertThat(record.s, is((short)32767));
            assertThat(record.i, is((int)2147483647));
            assertThat(record.l, is((long)9223372036854775807L));
            assertThat(record.f, is(Float.valueOf("3.4028234663852886E38")));
            assertThat(record.d, is(Double.valueOf("1.7976931348623157E308")));
            
        } else if(record.no == 17) {
            // 最大置+1（文字列型）
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
            assertThat(record.f, is(Float.valueOf("3.4028234663852887E38")));
            assertThat(record.d, is(Double.valueOf("1.7976931348623158E308")));
            
        } else if(record.no == 18) {
            // 最小値（文字列型）
            assertThat(record.b, is((byte)-128));
            assertThat(record.s, is((short)-32768));
            assertThat(record.i, is((int)-2147483648));
            assertThat(record.l, is((long)-9223372036854775808L));
            assertThat(record.f, is(Float.valueOf("-3.40282346638528E38")));
            assertThat(record.d, is(Double.valueOf("-1.7976931348623157E308")));
            
        } else if(record.no == 19) {
            // 最小置-1（文字列型）
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
            assertThat(record.f, is(Float.valueOf("-3.40282346638528E38")));
            assertThat(record.d, is(Double.valueOf("-1.7976931348623158E308")));
            
        } else if(record.no == 20) {
            // 最大置+1（数値型）
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
            assertThat(record.f, is(Float.valueOf("9.99999999999999E307")));
            assertThat(record.d, is(Double.valueOf("9.99999999999999E307")));
            
        } else if(record.no == 21) {
            // 最小置-1（数値型）
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
            assertThat(record.f, is(Float.valueOf("-9.99999999999999E307")));
            assertThat(record.d, is(Double.valueOf("-9.99999999999999E307")));
            
        } else if(record.no == 22) {
            // 数式
            assertThat(record.b, is((byte)2));
            assertThat(record.s, is((short)3));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)2.4));
            assertThat(record.f, is(Float.valueOf("4.85")));
            assertThat(record.d, is(Double.valueOf("24.25")));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    /**
     * ラッパークラスの値のチェック
     * @param record
     * @param errors
     */
    private void assertRecord(final WrapperRecord record, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), record.getClass().getSimpleName(), record.no, record.comment);
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.b, is(nullValue()));
            assertThat(record.s, is(nullValue()));
            assertThat(record.i, is(nullValue()));
            assertThat(record.l, is(nullValue()));
            assertThat(record.f, is(nullValue()));
            assertThat(record.d, is(nullValue()));
            
        } else if(record.no == 2) {
            // ゼロ（標準型）
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)0));
            assertThat(record.l, is((long)0L));
            assertThat(record.f, is((float)0.0));
            assertThat(record.d, is((double)0.0));
            
        } else if(record.no == 3) {
            // ゼロ（数値型）
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)0));
            assertThat(record.l, is((long)0L));
            assertThat(record.f, is((float)0.0));
            assertThat(record.d, is((double)0.0));
            
        } else if(record.no == 4) {
            // 正の数（標準）
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)12L));
            assertThat(record.f, is((float)12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 5) {
            // 正の数（数値型）
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)12L));
            assertThat(record.f, is((float)12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 6) {
            // 負の数（標準）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)-12.3));
            
        } else if(record.no == 7) {
            // 負の数（数値型）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)-12.3));
            
        } else if(record.no == 8) {
            // 数値（△ 1234）
            assertThat(record.b, is((byte)-12));
            assertThat(record.s, is((short)-12));
            assertThat(record.i, is((int)12));
            assertThat(record.l, is((long)-12L));
            assertThat(record.f, is((float)-12.3));
            assertThat(record.d, is((double)12.3));
            
        } else if(record.no == 9) {
            // 通貨
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)1234567L));
            assertThat(record.f, is((float)12345.67));
            assertThat(record.d, is((double)-12345.67));
            
        } else if(record.no == 10) {
            // 会計
            assertThat(record.b, is((byte)12));
            assertThat(record.s, is((short)12));
            assertThat(record.i, is((int)-12));
            assertThat(record.l, is((long)12346L));
            assertThat(record.f, is((float)12345.67));
            assertThat(record.d, is((double)-12345.67));
            
        } else if(record.no == 11) {
            // パーセント
            assertThat(record.b, is((byte)0));
            assertThat(record.s, is((short)0));
            assertThat(record.i, is((int)1));
            assertThat(record.l, is((long)-12));
            assertThat(record.f, is((float)0.1234));
            assertThat(record.d, is((double)-1.234567));
            
        } else if(record.no == 12) {
            // 分数
            assertThat(record.b, is((byte)1));
            assertThat(record.s, is((short)1));
            assertThat(record.i, is((int)-1));
            assertThat(record.l, is((long)123));
            assertThat(record.f, is((float)0.1234));
            assertThat(record.d, is((double)-1.234567));
            
        } else if(record.no == 13) {
            // 指数
            assertThat(record.b, is((byte)1));
            assertThat(record.s, is((short)1));
            assertThat(record.i, is((int)-1));
            assertThat(record.l, is((long)0));
            assertThat(record.f, is((float)0.0001));
            assertThat(record.d, is((double)1000.234));
            
        } else if(record.no == 14) {
            // 指数
            assertThat(record.b, is((byte)123));
            assertThat(record.s, is((short)123));
            assertThat(record.i, is((int)-123));
            assertThat(record.l, is((long)1234567));
            assertThat(record.f, is((float)123.456));
            assertThat(record.d, is((double)-123.456));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    /**
     * その他のクラスの値のチェック
     * @param record
     * @param errors
     */
    private void assertRecord(final OtherRecord record, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), record.getClass().getSimpleName(), record.no, record.comment);
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.bd, is(nullValue()));
            assertThat(record.bi, is(nullValue()));
            
        } else if(record.no == 2) {
            // ゼロ
            assertThat(record.bd, is(new BigDecimal("0")));
            assertThat(record.bi, is(new BigInteger("0")));
            
        } else if(record.no == 3) {
            // 正の数
            assertThat(record.bd, is(new BigDecimal("12")));
            assertThat(record.bi, is(new BigInteger("12")));
            
        } else if(record.no == 4) {
            // 負の数
            assertThat(record.bd, is(new BigDecimal("-12")));
            assertThat(record.bi, is(new BigInteger("-12")));
            
        } else if(record.no == 5) {
            // 小数
            assertThat(record.bd, is(new BigDecimal(Double.parseDouble("12.345"), new MathContext(15))));
            assertThat(record.bi, is(new BigInteger("12")));
            
        } else if(record.no == 6) {
            // 文字列
            assertThat(record.bd, is(new BigDecimal(Double.parseDouble("12.345")).setScale(3, RoundingMode.HALF_UP)));
            assertThat(record.bi, is(new BigInteger("12")));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    /**
     * 初期値、書式指定の値のチェック
     * @param record
     * @param errors
     */
    private void assertRecord(final FormattedRecord record, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), record.getClass().getSimpleName(), record.no, record.comment);
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.b, is((byte)1));
            assertThat(record.s, is((short)1));
            assertThat(record.i, is(nullValue()));
            assertThat(record.l, is((long)-1));
            assertThat(record.f, is(nullValue()));
            assertThat(record.d, is((double)10000.0));
            
        } else if(record.no == 2) {
            // 正の数
            assertThat(record.b, is((byte)123));
            assertThat(record.s, is((short)123));
            assertThat(record.i, is((int)1234));
            assertThat(record.l, is((long)1234567));
            assertThat(record.f, is((float)0.1234));
            assertThat(record.d, is((double)12345.67));
            
        } else if(record.no == 3) {
            // 負の数
            assertThat(record.b, is((byte)-123));
            assertThat(record.s, is((short)-123));
            assertThat(record.i, is((int)-1234));
            assertThat(record.l, is((long)-1234567));
            assertThat(record.f, is((float)-0.1234));
            assertThat(record.d, is((double)-12345.67));
            
        } else if(record.no == 4) {
            // 最大値
            assertThat(record.b, is((byte)127));
            assertThat(record.s, is((short)32767));
            assertThat(record.i, is((int)2147483647));
            assertThat(record.l, is((long)9223372036854775807L));
            
//            assertThat(record.f, is(Float.valueOf("3.4028234663852886E38")));
//            assertThat(record.d, is(Double.valueOf("1.7976931348623157E308")));
            
        } else if(record.no == 5) {
            // 最大置+1
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
//            assertThat(record.f, is(Float.valueOf("3.4028234663852887E38")));
//            assertThat(record.d, is(Double.valueOf("1.7976931348623158E308")));
            
        } else if(record.no == 6) {
            // 最小値
            assertThat(record.b, is((byte)-128));
            assertThat(record.s, is((short)-32768));
            assertThat(record.i, is((int)-2147483648));
            assertThat(record.l, is((long)-9223372036854775808L));
            
//            assertThat(record.f, is(Float.valueOf("-3.40282346638528E38")));
//            assertThat(record.d, is(Double.valueOf("-1.7976931348623157E308")));
            
        } else if(record.no == 7) {
            // 最小置-1
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            
//            assertThat(record.f, is(Float.valueOf("-3.40282346638528E38")));
//            assertThat(record.d, is(Double.valueOf("-1.7976931348623158E308")));
            
        } else if(record.no == 8) {
            // 不正な値（数値以外）
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("b"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("s"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("i"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("l"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("f"))).isTypeBindFailure(), is(true));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("d"))).isTypeBindFailure(), is(true));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    /**
     * Excelでは有効桁数は15桁なので、それ以上の数を書き込むと丸められる。
     */
    final long MAX_LONG = 999999999999999L;
    final long MIN_LONG = -999999999999999L;
    
    /**
     * Excelで処理可能な最大値は、数式で処理できる数よりも小さいので注意
     * ・有効桁数15桁になる
     */
    final double MAX_DOUBLE = 9.99999999999999E+307;
    final double MIN_DOUBLE = -9.99999999999999E+307;
    
    /**
     * 数値型の書き込みテスト
     */
    @Test
    public void test_save_number() throws Exception {
        
        // テストデータの作成
        NumberSheet outSheet = new NumberSheet();
        
        // プリミティブ型のデータ作成
        outSheet.add(new PrimitiveRecord()
                .comment("空文字"));
        
        outSheet.add(new PrimitiveRecord()
                .b(Byte.parseByte("0"))
                .s(Short.parseShort("0"))
                .i(0)
                .l(0L)
                .f(0.0f)
                .d(0.0)
                .comment("ゼロ"));
        
        outSheet.add(new PrimitiveRecord()
            .b(Byte.parseByte("12"))
            .s(Short.parseShort("12"))
            .i(12)
            .l(12L)
            .f(12.3f)
            .d(12.3)
            .comment("正の数"));
        
        outSheet.add(new PrimitiveRecord()
            .b(Byte.parseByte("-12"))
            .s(Short.parseShort("-12"))
            .i(-12)
            .l(-12L)
            .f(-12.3f)
            .d(-12.3)
            .comment("負の数"));
        
        outSheet.add(new PrimitiveRecord()
            .b(Byte.MAX_VALUE)
            .s(Short.MAX_VALUE)
            .i(Integer.MAX_VALUE)
            .l(MAX_LONG)
            .f(Float.MAX_VALUE)
            .d(MAX_DOUBLE)
            .comment("最大値"));
        
        outSheet.add(new PrimitiveRecord()
            .b(Byte.MIN_VALUE)
            .s(Short.MIN_VALUE)
            .i(Integer.MIN_VALUE)
            .l(MIN_LONG)
            .f(-Float.MAX_VALUE)
            .d(MIN_DOUBLE)
            .comment("最小値"));
        
        // ラッパークラス
        outSheet.add(new WrapperRecord()
                .comment("空文字"));
        
        outSheet.add(new WrapperRecord()
                .b(Byte.parseByte("0"))
                .s(Short.parseShort("0"))
                .i(0)
                .l(0L)
                .f(0.0f)
                .d(0.0)
                .comment("ゼロ"));
        
        outSheet.add(new WrapperRecord()
            .b(Byte.parseByte("12"))
            .s(Short.parseShort("12"))
            .i(12)
            .l(12L)
            .f(12.3f)
            .d(12.3)
            .comment("正の数"));
        
        outSheet.add(new WrapperRecord()
            .b(Byte.parseByte("-12"))
            .s(Short.parseShort("-12"))
            .i(-12)
            .l(-12L)
            .f(-12.3f)
            .d(-12.3)
            .comment("負の数"));
        
        outSheet.add(new WrapperRecord()
            .b(Byte.MAX_VALUE)
            .s(Short.MAX_VALUE)
            .i(Integer.MAX_VALUE)
            .l(MAX_LONG)
            .f(Float.MAX_VALUE)
            .d(MAX_DOUBLE)
            .comment("最大値"));
        
        outSheet.add(new WrapperRecord()
            .b(Byte.MIN_VALUE)
            .s(Short.MIN_VALUE)
            .i(Integer.MIN_VALUE)
            .l(MIN_LONG)
            .f(-Float.MAX_VALUE)
            .d(MIN_DOUBLE)
            .comment("最小値"));
        
        // その他のクラス
        outSheet.add(new OtherRecord()
                .comment("空文字"));
        
        outSheet.add(new OtherRecord()
            .bd(BigDecimal.valueOf(0))
            .bi(BigInteger.valueOf(0))
            .comment("ゼロ"));
        
        outSheet.add(new OtherRecord()
            .bd(new BigDecimal(12.3, new MathContext(15, RoundingMode.HALF_UP)))
            .bi(BigInteger.valueOf(12))
            .comment("正の数"));
        
        outSheet.add(new OtherRecord()
            .bd(new BigDecimal(-12.3, new MathContext(15, RoundingMode.HALF_UP)))
            .bi(BigInteger.valueOf(-12))
            .comment("負の数"));
        
        // 初期値、書式指定
        outSheet.add(new FormattedRecord()
            .comment("空文字"));
        
        outSheet.add(new FormattedRecord()
            .b(Byte.parseByte("12"))
            .s(Short.parseShort("12"))
            .i(12)
            .l(12L)
            .f(12.3f)
            .d(12.3)
            .comment("正の数"));
        
        outSheet.add(new FormattedRecord()
            .b(Byte.parseByte("-12"))
            .s(Short.parseShort("-12"))
            .i(-12)
            .l(-12L)
            .f(-12.3f)
            .d(-12.3)
            .comment("負の数"));
        
        outSheet.add(new FormattedRecord()
            .b(Byte.MAX_VALUE)
            .s(Short.MAX_VALUE)
            .i(Integer.MAX_VALUE)
            .l(MAX_LONG)
            .f(Float.MAX_VALUE)
            .d(MAX_DOUBLE)
            .comment("最大値"));
        
        outSheet.add(new FormattedRecord()
            .b(Byte.MIN_VALUE)
            .s(Short.MIN_VALUE)
            .i(Integer.MIN_VALUE)
            .l(MIN_LONG)
            .f(-Float.MAX_VALUE)
            .d(MIN_DOUBLE)
            .comment("最小値"));
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        File outFile = new File("src/test/out/convert_number.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/convert_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(NumberSheet.class);
            
            NumberSheet sheet = mapper.load(in, NumberSheet.class, errors);
            
            if(sheet.primitiveRecords != null) {
                assertThat(sheet.primitiveRecords, hasSize(outSheet.primitiveRecords.size()));
                
                for(int i=0; i < sheet.primitiveRecords.size(); i++) {
                    assertRecord(sheet.primitiveRecords.get(i), outSheet.primitiveRecords.get(i), errors);
                }
            }
            
            if(sheet.wrapperRecords != null) {
                assertThat(sheet.wrapperRecords, hasSize(outSheet.wrapperRecords.size()));
                
                for(int i=0; i < sheet.wrapperRecords.size(); i++) {
                    assertRecord(sheet.wrapperRecords.get(i), outSheet.wrapperRecords.get(i), errors);
                }
            }
            
            if(sheet.otherRecords != null) {
                assertThat(sheet.otherRecords, hasSize(outSheet.otherRecords.size()));
                
                for(int i=0; i < sheet.otherRecords.size(); i++) {
                    assertRecord(sheet.otherRecords.get(i), outSheet.otherRecords.get(i), errors);
                }
            }
            
            if(sheet.formattedRecords != null) {
                assertThat(sheet.formattedRecords, hasSize(outSheet.formattedRecords.size()));
                
                for(int i=0; i < sheet.formattedRecords.size(); i++) {
                    assertRecord(sheet.formattedRecords.get(i), outSheet.formattedRecords.get(i), errors);
                }
            }
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final PrimitiveRecord inRecord, final PrimitiveRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        assertThat(inRecord.no, is(outRecord.no));
        assertThat(inRecord.b, is(outRecord.b));
        assertThat(inRecord.s, is(outRecord.s));
        assertThat(inRecord.i, is(outRecord.i));
        assertThat(inRecord.l, is(outRecord.l));
        assertThat(inRecord.f, is(outRecord.f));
        assertThat(inRecord.d, is(outRecord.d));
        assertThat(inRecord.comment, is(outRecord.comment));
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final WrapperRecord inRecord, final WrapperRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.b, is(nullValue()));
            assertThat(inRecord.s, is(nullValue()));
            assertThat(inRecord.i, is(nullValue()));
            assertThat(inRecord.l, is(nullValue()));
            assertThat(inRecord.f, is(nullValue()));
            assertThat(inRecord.d, is(nullValue()));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.b, is(outRecord.b));
            assertThat(inRecord.s, is(outRecord.s));
            assertThat(inRecord.i, is(outRecord.i));
            assertThat(inRecord.l, is(outRecord.l));
            assertThat(inRecord.f, is(outRecord.f));
            assertThat(inRecord.d, is(outRecord.d));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final OtherRecord inRecord, final OtherRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.bd, is(nullValue()));
            assertThat(inRecord.bi, is(nullValue()));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.bd, is(outRecord.bd));
            assertThat(inRecord.bi, is(outRecord.bi));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final FormattedRecord inRecord, final FormattedRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.b, is(Byte.valueOf("1")));
            assertThat(inRecord.s, is(Short.valueOf("1")));
            assertThat(inRecord.i, is(nullValue()));
            assertThat(inRecord.l, is(-1l));
            assertThat(inRecord.f, is(nullValue()));
            assertThat(inRecord.d, is(10000.0));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.b, is(outRecord.b));
            assertThat(inRecord.s, is(outRecord.s));
            assertThat(inRecord.i, is(outRecord.i));
            assertThat(inRecord.l, is(outRecord.l));
            assertThat(inRecord.f, is(outRecord.f));
            assertThat(inRecord.d, is(outRecord.d));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        }
        
    }
    
    @XlsSheet(name="数値型")
    private static class NumberSheet {
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="プリミティブ型", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<PrimitiveRecord> primitiveRecords;
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="ラッパークラス", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<WrapperRecord> wrapperRecords;
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="その他のクラス", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<OtherRecord> otherRecords;
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="初期値、書式指定", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<FormattedRecord> formattedRecords;
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public NumberSheet add(PrimitiveRecord record) {
            if(primitiveRecords == null) {
                this.primitiveRecords = new ArrayList<>();
            }
            this.primitiveRecords.add(record);
            record.no(primitiveRecords.size());
            return this;
        }
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public NumberSheet add(WrapperRecord record) {
            if(wrapperRecords == null) {
                this.wrapperRecords = new ArrayList<>();
            }
            this.wrapperRecords.add(record);
            record.no(wrapperRecords.size());
            return this;
        }
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public NumberSheet add(OtherRecord record) {
            if(otherRecords == null) {
                this.otherRecords = new ArrayList<>();
            }
            this.otherRecords.add(record);
            record.no(otherRecords.size());
            return this;
        }
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public NumberSheet add(FormattedRecord record) {
            if(formattedRecords == null) {
                this.formattedRecords = new ArrayList<>();
            }
            this.formattedRecords.add(record);
            record.no(formattedRecords.size());
            return this;
        }
        
    }
    
    /**
     * 数値型 - プリミティブ型
     * 
     */
    private static class PrimitiveRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="byte型")
        private byte b;
        
        @XlsColumn(columnName="short型")
        private short s;
        
        @XlsColumn(columnName="int型")
        private int i;
        
        @XlsColumn(columnName="long型")
        private long l;
        
        @XlsColumn(columnName="float型")
        private float f;
        
        @XlsColumn(columnName="double型")
        private double d;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public PrimitiveRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public PrimitiveRecord b(byte b) {
            this.b = b;
            return this;
        }
        
        public PrimitiveRecord s(short s) {
            this.s = s;
            return this;
        }
        
        public PrimitiveRecord i(int i) {
            this.i = i;
            return this;
        }
        
        public PrimitiveRecord l(long l) {
            this.l = l;
            return this;
        }
        
        public PrimitiveRecord f(float f) {
            this.f = f;
            return this;
        }
        
        public PrimitiveRecord d(double d) {
            this.d = d;
            return this;
        }
        
        public PrimitiveRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
    }
    
    /**
     * 数値型 - ラッパークラス
     *
     */
    private static class WrapperRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="Byteクラス")
        private Byte b;
        
        @XlsColumn(columnName="Shortクラス")
        private Short s;
        
        @XlsColumn(columnName="Integerクラス")
        private Integer i;
        
        @XlsColumn(columnName="Longクラス")
        private Long l;
        
        @XlsColumn(columnName="Floatクラス")
        private Float f;
        
        @XlsColumn(columnName="Doubleクラス")
        private Double d;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public WrapperRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public WrapperRecord b(Byte b) {
            this.b = b;
            return this;
        }
        
        public WrapperRecord s(Short s) {
            this.s = s;
            return this;
        }
        
        public WrapperRecord i(Integer i) {
            this.i = i;
            return this;
        }
        
        public WrapperRecord l(Long l) {
            this.l = l;
            return this;
        }
        
        public WrapperRecord f(Float f) {
            this.f = f;
            return this;
        }
        
        public WrapperRecord d(Double d) {
            this.d = d;
            return this;
        }
        
        public WrapperRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
    /**
     * 数値型  - その他のクラス
     *
     */
    private static class OtherRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="BigDecimalクラス")
        private BigDecimal bd;
        
        @XlsColumn(columnName="BigIntegerクラス")
        private BigInteger bi;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public OtherRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public OtherRecord bd(BigDecimal bd) {
            this.bd = bd;
            return this;
        }
        
        public OtherRecord bi(BigInteger bi) {
            this.bi = bi;
            return this;
        }
        
        public OtherRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
    /**
     * 数値型 - 初期値、書式指定
     *
     */
    private static class FormattedRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        /** 初期値 */
        @XlsConverter(defaultValue="1")
        @XlsColumn(columnName="byte型")
        private Byte b;
        
        /** 初期値 */
        @XlsConverter(defaultValue="1")
        @XlsColumn(columnName="short型")
        private Short s;
        
        /** 通貨 */
        @XlsNumberConverter(javaPattern="", currency="JPY", locale="ja_JP")
        @XlsColumn(columnName="int型")
        private Integer i;
        
        /** トリム */
        @XlsConverter(defaultValue="-1", trim=true)
        @XlsNumberConverter(javaPattern="#,###,##0")
        @XlsColumn(columnName="long型")
        private Long l;
        
        /** パーセント */
        @XlsNumberConverter(javaPattern="##.00%", excelPattern="##.00%")
        @XlsColumn(columnName="float型")
        private Float f;
        
        /** 正と負の数 */
        @XlsConverter(defaultValue="10,000.00")
        @XlsNumberConverter(javaPattern="#,##0.00;(#,##0.00)", excelPattern="#,##0.00;(#,##0.00)")
        @XlsColumn(columnName="double型")
        private Double d;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public FormattedRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public FormattedRecord b(byte b) {
            this.b = b;
            return this;
        }
        
        public FormattedRecord s(short s) {
            this.s = s;
            return this;
        }
        
        public FormattedRecord i(int i) {
            this.i = i;
            return this;
        }
        
        public FormattedRecord l(long l) {
            this.l = l;
            return this;
        }
        
        public FormattedRecord f(float f) {
            this.f = f;
            return this;
        }
        
        public FormattedRecord d(double d) {
            this.d = d;
            return this;
        }
        
        public FormattedRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
}
