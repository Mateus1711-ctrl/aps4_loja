package br.insper.loja.compra;

import br.insper.loja.evento.EventoService;
import br.insper.loja.produto.ProdutoService;
import br.insper.loja.produto.Produto;
import br.insper.loja.usuario.Usuario;
import br.insper.loja.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;

    // Utilizando o ProdutoService (serviço de integração com o microserviço de Produto)
    @Autowired
    private ProdutoService produtoService;

    public Compra salvarCompra(Compra compra) {
        Usuario usuario = usuarioService.getUsuario(compra.getUsuario());
        compra.setNome(usuario.getNome());
        compra.setDataCompra(LocalDateTime.now());

        // Validação dos produtos e cálculo do total
        List<String> listaIds = compra.getProdutos();
        if (listaIds == null || listaIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum produto informado para a compra");
        }

        // Conta a frequência de cada produto
        Map<String, Integer> contagemProdutos = new HashMap<>();
        for (String prodId : listaIds) {
            contagemProdutos.put(prodId, contagemProdutos.getOrDefault(prodId, 0) + 1);
        }

        double totalCompra = 0;
        // Para cada produto, valida estoque e acumula o preço
        for (Map.Entry<String, Integer> entry : contagemProdutos.entrySet()) {
            String prodId = entry.getKey();
            int quantidadeComprada = entry.getValue();
            Produto produto = produtoService.getProduto(prodId);
            if (produto.getQuantidade() < quantidadeComprada) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Estoque insuficiente para o produto " + prodId);
            }
            totalCompra += produto.getPreco() * quantidadeComprada;
        }

        // Decrementa o estoque para cada produto comprado
        for (Map.Entry<String, Integer> entry : contagemProdutos.entrySet()) {
            produtoService.decrementarEstoque(entry.getKey(), entry.getValue());
        }

        // Define o total da compra
        compra.setTotal(totalCompra);

        // Registra o evento de compra
        eventoService.salvarEvento(usuario.getEmail(), "Compra realizada");
        return compraRepository.save(compra);
    }

    public List<Compra> getCompras() {
        return compraRepository.findAll();
    }
}
