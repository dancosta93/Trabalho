package dacost.com.trabalho;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private final String QR_CODE_FORMAT = "QR_CODE";

    private DataBaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = new DataBaseHelper(this);
    }

    /**
     * Abre a activity de produtos, lendo primeiramente um barcode.
     * Se identificar um produto com aquele barcode, carrega os dados do produto.
     * Se nao identificar, cadastra um novo produto
     *
     * @param v
     */
    public void readBarCode(View v) {
        Intent i = new Intent(v.getContext(), ProdutoActivity.class);
        startActivity(i);
    }


    /**
     * Abre o QR code reader.
     *
     * @param v
     */
    public void readQrCode(View v) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    /**
     * Processa o retorno do QR code reader
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            String content = scanningResult.getContents();
            if (content != null && !content.isEmpty()) {
                try {
                    Log.d(TAG, "Tipo :" + scanningResult.getFormatName());
                    if (scanningResult.getFormatName().equals(QR_CODE_FORMAT)) {
                        this.processQrCode(scanningResult.getContents());
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Formato Inválido. Precisa ser um QR Code!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Leitura Invalida!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Leitura Invalida!", Toast.LENGTH_SHORT);
                toast.show();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Leitura Invalida!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void processQrCode(String content) {
        Uri uri = Uri.parse(content);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }
}
