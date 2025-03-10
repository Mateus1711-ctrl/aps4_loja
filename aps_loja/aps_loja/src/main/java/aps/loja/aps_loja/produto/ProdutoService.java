package br.insper.loja.produto;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ProdutoService {

    private final RestTemplate restTemplate = new RestTemplate();
    // URL base do serviço de produto – ajuste a porta se necessário
    private final String produtoServiceUrl = "http://localhost:8080/api/produto";

    public Produto getProduto(String id) {
        try {
            return restTemplate.getForObject(produtoServiceUrl + "/" + id, Produto.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado: " + id);
        }
    }

    public Produto decrementarEstoque(String id, int quantidade) {
        try {
            ResponseEntity<Produto> response = restTemplate.exchange(
                    produtoServiceUrl + "/" + id + "/decrement?quantidade=" + quantidade,
                    HttpMethod.PUT,
                    HttpEntity.EMPTY,
                    Produto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao decrementar estoque para o produto: " + id);
        }
    }
}
