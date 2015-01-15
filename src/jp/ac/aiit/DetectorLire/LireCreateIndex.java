
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class LireCreateIndex {

	// 分析対象フォルダ
	private final static String TARGET_DIR = "/Users/kakuhrf/Documents/photos/favor";

	// 一時中間データ保存フォルダ
	private final static String INDEX_PATH = "/Users/kakuhrf/Documents/lire/index";
	
	// 画像違い度 （0になたら、完全に類似）
	private final static int DIFF_LEVEL = 30;
		
	public static void main(String[] args) {
		
        // 処理開始時間を取得します。
        long startTime = System.currentTimeMillis();
        // グループ計数
		int count = 0;
		
	    IndexReader reader = null;
	    ImageSearcher searcher = null;
		BufferedImage biImg = null;
		ImageSearchHits hits = null;
		List<String> fileNameList = new ArrayList<String>();
		Map<String, File> fileMap = new HashMap<String, File>();
		
		// 分析対象フォルダについて分析中間データ作成
		int indexCount = DetectorUtil.imageIndexing(INDEX_PATH, TARGET_DIR, true);
		//System.out.println(indexCount);
		// 重複抽出準備
		if (indexCount > 0) {
			File[] fileLists = new java.io.File(TARGET_DIR).listFiles();
			for (File f : fileLists) {
				if (f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".JPG")) {
					fileNameList.add(f.getName());
					fileMap.put(f.getName(), f);
				}
			}
			
		    try {
				reader = IndexReader.open(FSDirectory.open(new File(INDEX_PATH)));
				searcher = ImageSearcherFactory.createCEDDImageSearcher(50);
				
				while(fileNameList.size() > 0) {
					File obj = (File)fileMap.get(fileNameList.get(0));
					biImg = DetectorUtil.loadImage(obj);	
					hits = searcher.search(biImg, reader);
					// 似ている画像をコンソールに出力する
					System.out.println("重複画像グループ" + count);
					for (int i = 0; i < hits.length(); i++) {
						// 類似度より抽出する
						if (hits.score(i) < DIFF_LEVEL) {
							System.out
							.println(hits.score(i)
									+ ": "
									+ hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER)
											.stringValue());
							fileNameList.remove(DetectorUtil.getSuffix(hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue()));
							
						}
					}
					count++;	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
		
        // 処理終了時間を取得します
        long endTime = System.currentTimeMillis();
        
        // 処理終了時間から処理開始時間を差し引いてミリ秒で処理時間を表示します
        System.out.println("処理時間：" + (endTime - startTime)  + "ms");
	}

}
