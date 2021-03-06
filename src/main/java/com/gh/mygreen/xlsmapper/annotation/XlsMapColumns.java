package com.gh.mygreen.xlsmapper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.gh.mygreen.xlsmapper.XlsMapperConfig;

/**
 * アノテーション{@link XlsHorizontalRecords}や{@link XlsVerticalRecords}で指定されたレコード用のクラスにおいて、
 * カラム数が可変の場合にそれらのカラムを{@link java.util.Map}として設定します。
 * 
 * <p>BeanにはMapを引数に取るフィールドまたはメソッドを用意し、このアノテーションを記述します。</p>
 * 
 * <h3 class="description">基本的な使い方</h3>
 * <p>属性{@link #previousColumnName()}で指定された次のカラム以降、カラム名をキーとした{@link java.util.Map}が生成され、Beanにセットされます。</p>
 * <p>マップのキーは必ず{@link String}型に設定してください。</p>
 * 
 * <pre class="highlight"><code class="java">
 * public class SampleRecord {
 *     
 *     {@literal @XlsColumn(columnName="ID")}
 *     private int id;
 *     
 *     {@literal @XlsColumn(columnName="名前")}
 *     private String name;
 *     
 *     {@literal @XlsMapColumns(previousColumnName="名前")}
 *     private {@literal Map<String, String>} attendedMap;
 *     
 * }
 * </code></pre>
 * 
 * <div class="picture">
 *    <img src="doc-files/MapColumns.png">
 *    <p>基本的な使い方</p>
 * </div>
 *
 *
 * <h3 class="description">終了条件のセルを指定する場合</h3>
 * <p>属性{@link #nextColumnName()}で指定した前のカラムまでが処理対象となり、マッピングの終了条件を指定することができます。</p>
 * 
 * <pre class="highlight"><code class="java">
 * public class SampleRecord {
 *     
 *     {@literal @XlsColumn(columnName="ID")}
 *     private int id;
 *     
 *     {@literal @XlsColumn(columnName="名前")}
 *     private String name;
 *     
 *     {@literal @XlsMapColumns(previousColumnName="名前", nextColumnName="備考")}
 *     private {@literal Map<String, String>} attendedMap;
 *     
 *     {@literal @XlsColumn(columnName="備考")}
 *     private String comment;
 *     
 * }
 * </code></pre>
 * 
 * <div class="picture">
 *    <img src="doc-files/MapColumns_nextColumnName.png">
 *    <p>マッピングの終了条件の指定</p>
 * </div>

 *
 *
 * <h3 class="description">型変換する場合</h3>
 * <p>アノテーション{@link com.gh.mygreen.xlsmapper.annotation.XlsConverter}などで型変換を適用するときは、Mapの値が変換対象となります。
 *    <br>マップのキーは必ず{@link String}型に設定してください
 * </p>
 * 
 * <pre class="highlight"><code class="java">
 * public class SampleRecord {
 *     
 *     {@literal @XlsColumn(columnName="ID")}
 *     private int id;
 *     
 *     {@literal @XlsColumn(columnName="名前")}
 *     private String name;
 *     
 *     // 型変換用のアノテーションを指定した場合、Mapの値に適用されます。
 *     {@literal @XlsMapColumns(previousColumnName="名前")}
 *     {@literal @XlsBooleanConverter(loadForTrue={"出席"}, loadForFalse={"欠席"},
 *             saveAsTrue="出席", saveAsFalse"欠席"
 *             failToFalse=true)}
 *     private {@literal Map<String, Boolean>} attendedMap;
 *     
 * }
 * </code></pre>
 *
 * <h3 class="description">位置情報／見出し情報を取得する際の注意事項</h3>
 * <p>マッピング対象のセルのアドレスを取得する際に、フィールド{@literal Map<String, Point> positions}を定義しておけば、
 *    自動的にアドレスがマッピングされます。
 *    <br>通常は、キーにはプロパティ名が記述（フィールドの場合はフィールド名）が入ります。
 *    <br>アノテーション{@link XlsMapColumns}でマッピングしたセルのキーは、{@literal <プロパティ名>[<セルの見出し>]}の形式になります。
 * </p>
 * <p>同様に、マッピング対象の見出しを取得する、フィールド{@literal Map<String, String> labels}へのアクセスも、
 *    キーは{@literal <プロパティ名>[<セルの見出し>]}の形式になります。
 * </p>
 * 
 * <pre class="highlight"><code class="java">
 * public class SampleRecord {
 *     
 *     // 位置情報
 *     private {@literal Map<String, Point>} positions;
 *     
 *     // 見出し情報
 *     private {@literal Map<String, String>} labels;
 *     
 *     {@literal @XlsColumn(columnName="ID")}
 *     private int id;
 *     
 *     {@literal @XlsColumn(columnName="名前")}
 *     private String name;
 *     
 *     {@literal @XlsMapColumns(previousColumnName="名前")}
 *     private {@literal Map<String, String>} attendedMap;
 *     
 * }
 * 
 * 
 * // 位置情報・見出し情報へのアクセス
 * SampleRecord record = ...;
 * 
 * Point position = record.positions.get("attendedMap[4月2日]");
 * 
 * String label = recrod.labeles.get("attendedMap[4月2日]");
 * </code></pre>
 * 
 * 
 * <div class="picture">
 *    <img src="doc-files/MapColumns_positions.png">
 *    <p>位置情報・見出し情報の取得</p>
 * </div>
 * 
 * 
 * <h3 class="description">見出しを正規表現、正規化して指定する場合</h3>
 * 
 * <p>シートの構造は同じだが、ラベルのセルが微妙に異なる場合、ラベルセルを正規表現による指定が可能です。
 *   <br>また、空白や改行を除去してラベルセルを比較するように設定することも可能です。</p>
 * 
 * <p>正規表現で指定する場合、アノテーションの属性の値を {@code /正規表現/} のように、スラッシュで囲みます。</p>
 * <ul>
 *   <li>スラッシュで囲まない場合、通常の文字列として処理されます。</li>
 *   <li>正規表現の指定機能を有効にするには、システム設定のプロパティ {@link XlsMapperConfig#setRegexLabelText(boolean)} の値を trueに設定します。</li>
 * </ul>
 * 
 * <p>ラベセルの値に改行が空白が入っている場合、それらを除去し正規化してアノテーションの属性値と比較することが可能です。</p>
 * <ul>
 *   <li>正規化とは、空白、改行、タブを除去することを指します。</li>
 *   <li>ラベルを正規化する機能を有効にするには、、システム設定のプロパティ {@link XlsMapperConfig#setNormalizeLabelText(boolean)} の値を trueに設定します。</li>
 * </ul>
 * 
 * <p>これらの指定が可能な属性は、{@link #previousColumnName()}、{@link #nextColumnName()}です。</p>
 * 
 * <pre class="highlight"><code class="java">
 * // システム設定
 * XlsMapper xlsMapper = new XlsMapper();
 * xlsMapper.getConfig()
 *         .setRegexLabelText(true)        // ラベルを正規表現で指定可能にする機能を有効にする。
 *         .setNormalizeLabelText(true);   // ラベルを正規化して比較する機能を有効にする。
 * 
 * // レコード用クラス
 * public class SampleRecord {
 *     
 *     {@literal @XlsColumn(columnName="ID")}
 *     private int id;
 *     
 *     // 正規表現による指定
 *     {@literal @XlsColumn(columnName="/名前.+/")}
 *     private String name;
 *     
 *     // 正規表現による指定
 *     {@literal @XlsMapColumns(previousColumnName="/名前.+/", nextColumnName="/備考.+/")}
 *     private Map<String, String> attendedMap;
 *     
 *     {@literal @XlsColumn(columnName="/備考.+/")}
 *     private String comment;
 *     
 * }
 * </code></pre>
 *
 * @author Naoki Takezoe
 * @author T.TSUCHIE
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XlsMapColumns {
    
    /**
     * この属性で指定した次のカラム以降、カラム名をキーとしたMapが生成され、Beanにセットされます。
     * <p>システム設定により、正規表現による指定や正規化（改行、空白、タブの削除）による比較の対象となります。</p>
     * @return
     */
    String previousColumnName();
    
    /**
     * この属性で指定した前のカラムまでが処理対象となり、マッピングの終了条件を指定することができます。
     * <p>システム設定により、正規表現による指定や正規化（改行、空白、タブの削除）による比較の対象となります。</p>
     * 
     * @since 1.2
     * @return
     */
    String nextColumnName() default "";
    
    /** 
     * マップの値のクラスを指定します。
     * <p>省略した場合、定義されたたGenericsの情報から取得します。
     */
    Class<?> itemClass() default Object.class;
}
