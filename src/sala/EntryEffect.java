package sala;

public interface EntryEffect {
    void happen(Ref<Integer> r, Token it, Token[] args) throws SalaException;
}
