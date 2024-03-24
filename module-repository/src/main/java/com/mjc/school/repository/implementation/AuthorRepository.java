package com.mjc.school.repository.implementation;



import com.mjc.school.repository.model.AuthorModel;




import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Optional;

@Repository("authorRepository")
public class AuthorRepository extends AbstractDBRepository <AuthorModel, Long>  {


    @Override
    void update(AuthorModel prevState, AuthorModel nextState) {
        if(nextState.getName()!=null && !nextState.getName().isBlank()){
            prevState.setName(nextState.getName());
        }
    }

    public Optional<AuthorModel> readAuthorByNewsId(Long newsId) {
        Optional<AuthorModel> result = Optional.of(entityManager.createQuery("SELECT a FROM AuthorModel a INNER JOIN a.newsModelListWithId b WHERE b.id=:newsId", AuthorModel.class).setParameter("newsId", newsId).getSingleResult());
        return result;
    }
    public Optional<AuthorModel> readAuthorByName(String name){
        TypedQuery<AuthorModel> typedQuery = entityManager.createQuery("SELECT a FROM AuthorModel a WHERE a.name LIKE:name", AuthorModel.class).setParameter("name", "%" + name + "%");
        return Optional.of(typedQuery.getSingleResult());
    }
}