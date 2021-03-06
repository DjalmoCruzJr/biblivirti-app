package org.sysmob.biblivirti.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;
import org.sysmob.biblivirti.R;
import org.sysmob.biblivirti.application.BiblivirtiApplication;
import org.sysmob.biblivirti.business.AccountBO;
import org.sysmob.biblivirti.exceptions.ValidationException;
import org.sysmob.biblivirti.model.Usuario;
import org.sysmob.biblivirti.network.ITransaction;
import org.sysmob.biblivirti.network.NetworkConnection;
import org.sysmob.biblivirti.network.RequestData;
import org.sysmob.biblivirti.utils.BiblivirtiConstants;
import org.sysmob.biblivirti.utils.BiblivirtiDialogs;
import org.sysmob.biblivirti.utils.BiblivirtiParser;
import org.sysmob.biblivirti.utils.BiblivirtiUtils;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private Button buttonConfirmarEmail;

    private EditText editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        // habilita botao de voltar na acitonbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Carrega os widgets da tela
        loadWidgets();

        // Carrega os listeners dos widgets
        loadListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                BiblivirtiApplication.getInstance().cancelPendingRequests(this.getClass().getSimpleName());
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*****************************************************
     * PRIVATE METHODS
     *****************************************************/
    private void enableWidgets(boolean status) {
        this.editEmail.setEnabled(status);
        this.buttonConfirmarEmail.setEnabled(status);
    }

    private void loadWidgets() {
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.editEmail = (EditText) findViewById(R.id.editEmail);
        this.buttonConfirmarEmail = (Button) findViewById(R.id.buttonConfirmarEmail);
    }

    private void loadListeners() {
        this.buttonConfirmarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BiblivirtiUtils.isNetworkConnected()) {
                    String message = "Você não está conectado a internet.\nPor favor, verifique sua conexão e tente novamente!";
                    Toast.makeText(RecuperarSenhaActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        if (new AccountBO(RecuperarSenhaActivity.this).validatePasswordReset()) {
                            Bundle fields = new Bundle();
                            fields.putString(Usuario.FIELD_USCMAIL, editEmail.getText().toString().trim());
                            actionConfirmarEmail(fields);
                        }
                    } catch (ValidationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadErrors(JSONObject errors) {
        try {
            getEditEmail().setError(errors.opt(Usuario.FIELD_USCMAIL) != null ? errors.getString(Usuario.FIELD_USCMAIL) : null);
        } catch (JSONException e) {
            Log.e(String.format("%s:", getClass().getSimpleName().toString()), e.getMessage());
            e.printStackTrace();
        }
    }

    /*****************************************************
     * PUBLIC METHODS
     *****************************************************/
    public EditText getEditEmail() {
        return editEmail;
    }

    public void setEditEmail(EditText editEmail) {
        this.editEmail = editEmail;
    }

    /*****************************************************
     * ACTION METHODS
     *****************************************************/
    public void actionConfirmarEmail(Bundle fields) {
        try {
            JSONObject params = new JSONObject();
            params.put(Usuario.FIELD_USCMAIL, fields.getString(Usuario.FIELD_USCMAIL));
            RequestData requestData = new RequestData(
                    this.getClass().getSimpleName(),
                    Request.Method.POST,
                    BiblivirtiConstants.API_ACCOUNT_RECOVERY,
                    params
            );
            new NetworkConnection(this).execute(requestData, new ITransaction() {
                @Override
                public void onBeforeRequest() {
                    progressBar.setVisibility(View.VISIBLE);
                    enableWidgets(false);
                }

                @Override
                public void onAfterRequest(JSONObject response) {
                    if (response == null) {
                        String message = "Não houve resposta do servidor.\nTente novamente e em caso de falha entre em contato com a equipe de suporte do Biblivirti.";
                        Toast.makeText(RecuperarSenhaActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            if (response.getInt(BiblivirtiConstants.RESPONSE_CODE) != BiblivirtiConstants.RESPONSE_CODE_OK) {
                                // Verifica se a conta do usuario ainda não foi confirmada
                                if (response.getInt(BiblivirtiConstants.RESPONSE_CODE) == BiblivirtiConstants.RESPONSE_CODE_UNAUTHORIZED) {
                                    String message = response.getString(BiblivirtiConstants.RESPONSE_MESSAGE);
                                    Toast.makeText(RecuperarSenhaActivity.this, message, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RecuperarSenhaActivity.this, ConfirmarEmailActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    BiblivirtiDialogs.showMessageDialog(
                                            RecuperarSenhaActivity.this,
                                            "Mensagem",
                                            String.format(
                                                    "Código: %d\n%s\n%s",
                                                    response.getInt(BiblivirtiConstants.RESPONSE_CODE),
                                                    response.getString(BiblivirtiConstants.RESPONSE_MESSAGE),
                                                    BiblivirtiUtils.createStringErrors(response.opt(BiblivirtiConstants.RESPONSE_ERRORS) != null ? response.getJSONObject(BiblivirtiConstants.RESPONSE_ERRORS) : null)
                                            ),
                                            "Ok"
                                    );
                                    // Carrega as mensagens de erro nos widgets
                                    loadErrors(response.getJSONObject(BiblivirtiConstants.RESPONSE_ERRORS));
                                }
                            } else {
                                final String responseMessage = response.getString(BiblivirtiConstants.RESPONSE_MESSAGE);
                                final JSONObject responseData = response.getJSONObject(BiblivirtiConstants.RESPONSE_DATA);
                                BiblivirtiDialogs.showMessageDialog(
                                        RecuperarSenhaActivity.this,
                                        "Mensagem",
                                        String.format(
                                                "Código: %d\n%s",
                                                response.getInt(BiblivirtiConstants.RESPONSE_CODE),
                                                response.getString(BiblivirtiConstants.RESPONSE_MESSAGE)
                                        ),
                                        "Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    Toast.makeText(RecuperarSenhaActivity.this, responseMessage, Toast.LENGTH_LONG).show();
                                                    Log.i(String.format("%s:", RecuperarSenhaActivity.class.getSimpleName().toString()), responseMessage);
                                                    Usuario usuario = BiblivirtiParser.parseToUsuario(responseData);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable(Usuario.KEY_USUARIO, usuario);
                                                    Intent intent = new Intent(RecuperarSenhaActivity.this, ConfirmarRecuperacaoActivity.class);
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);
                                                    finish();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                );
                            }
                        } catch (JSONException e) {
                            Log.e(String.format("%s:", getClass().getSimpleName().toString()), e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    enableWidgets(true);
                }

                @Override
                public void onAfterRequest(String response) {
                }
            });
        } catch (JSONException e) {
            Log.e(String.format("%s:", getClass().getSimpleName().toString()), e.getMessage());
            e.printStackTrace();
        }
    }
}
