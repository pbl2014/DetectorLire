package jp.ac.aiit.DetectorLire;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;


public class DetectorUtil {
	
	/**
	 * ファイル名から拡張子を返します。
	 * @param fileName ファイル名
	 * @return ファイルの拡張子
	 */
	public static String getSuffix(String fileName) {
		String fn = new String();
	    if (fileName == null)
	        return fn;
	    int point = fileName.lastIndexOf("/");
	    if (point != -1) {
	        fn = fileName.substring(point + 1);
	    }
	    return fn;
	}
	
    /**
     * ロード画像ファイル
     *
     * @param file 画像ファイル
     * @return BufferedImage 画像ファイルのBuffer
     */
    public static BufferedImage loadImage(File file) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi;
    }
	
    /**
     * 対象画像ファイルを分析する
     * 分析した画像ファイルの中間ファイルは、一時フォルダにおきます
     *
     * @param path 一時フォルダパス
     * @param targetDir 分析対象フォルダパス
     * @return 分析したファイル数
     */
    public static int imageIndexing(String path, String targetDir, boolean force) {
        int count = 0;
        boolean hasIndex = false;

        try {
            hasIndex = DirectoryReader.indexExists(FSDirectory.open(new File(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (force || (hasIndex == false)) {
            try {
                java.util.ArrayList<java.lang.String> images = getAllImages(new java.io.File(targetDir), true);
                IndexWriter iw = LuceneUtils.createIndexWriter(path, true);
                DocumentBuilder builder = DocumentBuilderFactory.getFullDocumentBuilder();
                for (String identifier : images) {
                    Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                    iw.addDocument(doc);
                    count++;
                }
                iw.commit();
                iw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return count;
    }
    

    /**
     * 対象フォルダから画像ファイル一覧を取得する
     *
     * @param directory 対象画像ファイルフォルダ
     * @return resultList 対象画像ファイルの名称リスト
     * @throws IOException
     */
    public static ArrayList<String> getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> resultList = new ArrayList<String>();
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".JPG")) && !file.getName().startsWith("tn_")) {
                resultList.add(file.getCanonicalPath());
            }
            if (descendIntoSubDirectories && file.isDirectory()) {
                ArrayList<String> tmp = getAllImages(file, true);
                if (tmp != null) {
                    resultList.addAll(tmp);
                }
            }
        }
        if (resultList.size() > 0) {
            return resultList;
        } else {
            return null;
        }
    }


}
