package com.spacebanana.funwithgeofence;

import com.spacebanana.funwithgeofence.geofence.GeofencePoint;
import com.spacebanana.funwithgeofence.mainmap.MainMap;
import com.spacebanana.funwithgeofence.mainmap.MainMapPresenter;
import com.spacebanana.funwithgeofence.repository.GeofenceRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.configuration.DefaultMockitoConfiguration;
import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;

import static com.spacebanana.funwithgeofence.PredicateUtils.check;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class MainMapPresenterTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
//    @Rule public TestSchedulerRule testSchedulerRule = new TestSchedulerRule();

    @Mock
    MainMap mainMap;
    @Mock
    GeofenceRepository repository;

    MainMapPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new MainMapPresenter(repository);
    }

    @Test
    public void testAddGeofence() {
        //TODO fix that
        when(repository.addGeofenceArea(90, 90, 200)).thenReturn(
                Observable.just(new GeofencePoint(90, 90, 200, true)
                ));

        TestObserver<GeofencePoint> testObserver = repository.addGeofenceArea(90, 90, 200).test();
        testObserver
                .assertNoErrors()
                .assertComplete()
                .assertValue(check(
                        l -> {
                            mainMap.showGeofenceArea(l.getLat(), l.getLon(), l.getRadius());
                            verify(mainMap, times(1)).showGeofenceArea(l.getLat(), l.getLon(), l.getRadius());
                            assertEquals(90.0d, l.getLat());
                        }
                ));
    }
}
