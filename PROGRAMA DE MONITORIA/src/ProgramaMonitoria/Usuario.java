package ProgramaMonitoria;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
    private String nome;
    private String email;
    private String senha;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public boolean autenticar(String email, String senha) {
        return this.email.equals(email) && this.senha.equals(senha);
    }
    
    
    public String getNome() { 
    	return nome; 
    }
    
    public String getEmail() {
    	return email; 
    }

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
    
    
}


