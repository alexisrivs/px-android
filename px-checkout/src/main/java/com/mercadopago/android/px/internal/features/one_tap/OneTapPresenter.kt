package com.mercadopago.android.px.internal.features.one_tap

import android.annotation.SuppressLint
import com.mercadolibre.android.cardform.internal.LifecycleListener
import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.addons.model.internal.Configuration
import com.mercadopago.android.px.addons.model.internal.Experiment
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration
import com.mercadopago.android.px.core.DynamicDialogCreator
import com.mercadopago.android.px.core.internal.TriggerableQueue
import com.mercadopago.android.px.internal.base.BasePresenterWithState
import com.mercadopago.android.px.internal.core.AuthorizationProvider
import com.mercadopago.android.px.internal.datasource.CustomOptionIdSolver
import com.mercadopago.android.px.internal.domain.CheckoutUseCase
import com.mercadopago.android.px.internal.domain.CheckoutWithNewCardUseCase
import com.mercadopago.android.px.internal.experiments.KnownExperiment
import com.mercadopago.android.px.internal.experiments.KnownVariant
import com.mercadopago.android.px.internal.experiments.ScrolledVariant
import com.mercadopago.android.px.internal.experiments.VariantHandler
import com.mercadopago.android.px.internal.features.AmountDescriptorViewModelFactory
import com.mercadopago.android.px.internal.features.generic_modal.ActionType
import com.mercadopago.android.px.internal.features.generic_modal.ActionTypeWrapper
import com.mercadopago.android.px.internal.features.generic_modal.FromModalToGenericDialogItem
import com.mercadopago.android.px.internal.features.one_tap.offline_methods.OfflineMethods.Companion.shouldLaunch
import com.mercadopago.android.px.internal.features.one_tap.slider.HubAdapter
import com.mercadopago.android.px.internal.features.pay_button.PayButton
import com.mercadopago.android.px.internal.features.pay_button.PayButton.OnReadyForPaymentCallback
import com.mercadopago.android.px.internal.features.pay_button.PayButton.StateChange
import com.mercadopago.android.px.internal.features.pay_button.PayButton.ViewTrackPathCallback
import com.mercadopago.android.px.internal.mappers.ConfirmButtonViewModelMapper
import com.mercadopago.android.px.internal.mappers.ElementDescriptorMapper
import com.mercadopago.android.px.internal.mappers.InstallmentViewModelMapper
import com.mercadopago.android.px.internal.mappers.PaymentMethodDescriptorMapper
import com.mercadopago.android.px.internal.mappers.SplitHeaderMapper
import com.mercadopago.android.px.internal.mappers.SummaryInfoMapper
import com.mercadopago.android.px.internal.mappers.SummaryViewModelMapper
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.AmountRepository
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository
import com.mercadopago.android.px.internal.repository.ChargeRepository
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.ExperimentsRepository
import com.mercadopago.android.px.internal.repository.ModalRepository
import com.mercadopago.android.px.internal.repository.OneTapItemRepository
import com.mercadopago.android.px.internal.repository.PayerComplianceRepository
import com.mercadopago.android.px.internal.repository.PayerCostSelectionRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.tracking.TrackingRepository
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.internal.util.CardFormWrapper
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.internal.view.AmountDescriptorView
import com.mercadopago.android.px.internal.view.SummaryDetailDescriptorMapper
import com.mercadopago.android.px.internal.view.experiments.ExperimentHelper
import com.mercadopago.android.px.internal.view.experiments.ExperimentHelper.getVariantFrom
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction.ActionController
import com.mercadopago.android.px.internal.viewmodel.SummaryModel
import com.mercadopago.android.px.internal.viewmodel.drawables.PaymentMethodDrawableItemMapper
import com.mercadopago.android.px.model.DiscountConfigurationModel
import com.mercadopago.android.px.model.PayerCost
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.exceptions.ApiException
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.FromExpressMetadataToPaymentConfiguration
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.model.one_tap.CheckoutBehaviour
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.ConfirmEvent
import com.mercadopago.android.px.tracking.internal.events.InstallmentsEventTrack
import com.mercadopago.android.px.tracking.internal.events.SuspendedFrictionTracker
import com.mercadopago.android.px.tracking.internal.events.SwipeOneTapEventTracker
import com.mercadopago.android.px.tracking.internal.events.TargetBehaviourEvent
import com.mercadopago.android.px.tracking.internal.mapper.FromApplicationToApplicationInfo
import com.mercadopago.android.px.tracking.internal.mapper.FromSelectedExpressMetadataToAvailableMethods
import com.mercadopago.android.px.tracking.internal.model.ConfirmData
import com.mercadopago.android.px.tracking.internal.model.TargetBehaviourTrackData
import com.mercadopago.android.px.tracking.internal.views.OneTapViewTracker

internal class OneTapPresenter(
    private val paymentSettingRepository: PaymentSettingRepository,
    private val disabledPaymentMethodRepository: DisabledPaymentMethodRepository,
    private val payerCostSelectionRepository: PayerCostSelectionRepository,
    private val applicationSelectionRepository: ApplicationSelectionRepository,
    private val discountRepository: DiscountRepository,
    private val amountRepository: AmountRepository,
    private val checkoutUseCase: CheckoutUseCase,
    private val checkoutWithNewCardUseCase: CheckoutWithNewCardUseCase,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val chargeRepository: ChargeRepository,
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val experimentsRepository: ExperimentsRepository,
    private val trackingRepository: TrackingRepository,
    private val customTextsRepository: CustomTextsRepository,
    private val oneTapItemRepository: OneTapItemRepository,
    payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val modalRepository: ModalRepository,
    private val customOptionIdSolver: CustomOptionIdSolver,
    private val paymentMethodDrawableItemMapper: PaymentMethodDrawableItemMapper,
    private val paymentMethodDescriptorMapper: PaymentMethodDescriptorMapper,
    private val summaryDetailDescriptorMapper: SummaryDetailDescriptorMapper,
    private val summaryInfoMapper: SummaryInfoMapper,
    private val elementDescriptorMapper: ElementDescriptorMapper,
    private val fromApplicationToApplicationInfo: FromApplicationToApplicationInfo,
    private val authorizationProvider: AuthorizationProvider,
    private val amountDescriptorViewModelFactory: AmountDescriptorViewModelFactory,
    tracker: MPTracker
) : BasePresenterWithState<OneTap.View, OneTapState>(tracker), OneTap.Presenter, AmountDescriptorView.OnClickListener {

    private var triggerableQueue: TriggerableQueue = TriggerableQueue()

    init {
        viewTrack = OneTapViewTracker(
            fromApplicationToApplicationInfo,
            oneTapItemRepository.value,
            paymentSettingRepository.checkoutPreference!!,
            discountRepository.getCurrentConfiguration(), escManagerBehaviour.escCardIds,
            payerPaymentMethodRepository.getIdsWithSplitAllowed(),
            disabledPaymentMethodRepository.value.size,
            experimentsRepository.getExperiments(Configuration.TrackingMode.NO_CONDITIONAL)
        )
    }

    override fun initState(): OneTapState {
        return OneTapState()
    }

    private fun onFailToRetrieveInitResponse(apiException: ApiException) {
        view.showError(MercadoPagoError.createNotRecoverable(apiException, ApiUtil.RequestOrigin.POST_INIT))
    }

    override fun loadViewModel() {
        val summaryInfo = summaryInfoMapper.map(paymentSettingRepository.checkoutPreference!!)
        val elementDescriptorModel = elementDescriptorMapper.map(summaryInfo)
        val oneTapItemList = oneTapItemRepository.value
        val summaryModels: List<SummaryModel> = SummaryViewModelMapper(
            discountRepository, amountRepository, elementDescriptorModel, this,
            chargeRepository, amountConfigurationRepository, customTextsRepository, summaryDetailDescriptorMapper,
            applicationSelectionRepository, amountDescriptorViewModelFactory
        ).map(oneTapItemList)
        val paymentModels = paymentMethodDescriptorMapper.map(oneTapItemList)
        val splitHeaderModels = SplitHeaderMapper(
            paymentSettingRepository.currency,
            amountConfigurationRepository
        ).map(oneTapItemList)
        val confirmButtonViewModels = ConfirmButtonViewModelMapper(
            disabledPaymentMethodRepository
        ).map(oneTapItemList)
        val model = HubAdapter.Model(paymentModels, summaryModels, splitHeaderModels, confirmButtonViewModels)
        view.configurePayButton(object : StateChange {
            override fun overrideStateChange(uiState: PayButton.State): Boolean {
                return overridePayButtonStateChange(uiState)
            }
        })
        view.configurePaymentMethodHeader(getVariants())
        view.showToolbarElementDescriptor(elementDescriptorModel)
        view.configureRenderMode(getVariants())
        view.configureAdapters(paymentSettingRepository.site, paymentSettingRepository.currency)
        view.updateAdapters(model)
        updateElements()
        view.updatePaymentMethods(paymentMethodDrawableItemMapper.map(oneTapItemList))
        if (shouldLaunch(oneTapItemList)) {
            view.showOfflineMethodsCollapsed()
        }
    }

    override fun attachView(view: OneTap.View) {
        super.attachView(view)
        initPresenter()
    }

    private fun initPresenter() {
        if (isViewAttached) {
            triggerableQueue.execute()
            loadViewModel()
        }
    }

    override fun onFreshStart() {
        triggerableQueue.enqueue {
            trackView()
        }
    }

    override fun onGetViewTrackPath(callback: ViewTrackPathCallback) {
        callback.call(viewTrack!!.getTrack()!!.path)
    }

    private fun getCurrentOneTapItem() = oneTapItemRepository.value[state.paymentMethodIndex]

    override fun cancel() {
        trackBack()
        view.cancel()
    }

    override fun onBack() {
        trackAbort()
    }

    private fun updateElementPosition(selectedPayerCost: Int) {
        payerCostSelectionRepository.save(customOptionIdSolver[getCurrentOneTapItem()], selectedPayerCost)
        updateElements()
    }

    override fun onInstallmentsRowPressed() {
        updateInstallments()
        view.animateInstallmentsList()
        val oneTapItem = getCurrentOneTapItem()
        val amountConfiguration = amountConfigurationRepository.getConfigurationSelectedFor(customOptionIdSolver[oneTapItem])
        track(InstallmentsEventTrack(oneTapItem, amountConfiguration!!))
    }

    override fun updateInstallments() {
        val oneTapItem = getCurrentOneTapItem()
        val customOptionId = customOptionIdSolver[oneTapItem]
        val amountConfiguration = amountConfigurationRepository.getConfigurationSelectedFor(customOptionId)
        val payerCostList = getCurrentPayerCosts()
        val selectedIndex = amountConfiguration!!.getCurrentPayerCostIndex(
            state.splitSelectionState.userWantsToSplit(),
            payerCostSelectionRepository[customOptionId]
        )
        val models = InstallmentViewModelMapper(
            paymentSettingRepository.currency, oneTapItem.benefits,
            getVariants()
        ).map(payerCostList)
        view.updateInstallmentsList(selectedIndex, models)
    }

    private fun getCurrentPayerCosts(): List<PayerCost> {
        val oneTapItem = getCurrentOneTapItem()
        val amountConfiguration = amountConfigurationRepository.getConfigurationSelectedFor(customOptionIdSolver[oneTapItem])
        return amountConfiguration?.getAppliedPayerCost(state.splitSelectionState.userWantsToSplit()) ?: emptyList()
    }

    /**
     * When user cancel the payer cost selection this method will be called with the current payment method position
     */
    override fun onInstallmentSelectionCanceled() {
        updateElements()
        view.collapseInstallmentsSelection()
    }

    /**
     * When user selects a new payment method this method will be called with the new current paymentMethodIndex.
     *
     * @param paymentMethodIndex current payment method paymentMethodIndex.
     */
    override fun onSliderOptionSelected(paymentMethodIndex: Int) {
        state.paymentMethodIndex = paymentMethodIndex
        track(SwipeOneTapEventTracker())
        updateElementPosition(payerCostSelectionRepository[customOptionIdSolver[getCurrentOneTapItem()]])
    }

    private fun updateElements() {
        val customOptionId = customOptionIdSolver[getCurrentOneTapItem()]
        view.updateViewForPosition(
            state.paymentMethodIndex,
            payerCostSelectionRepository[customOptionId],
            state.splitSelectionState,
            applicationSelectionRepository[customOptionId]
        )
    }

    /**
     * When user selects a new payer cost for certain payment method this method will be called.
     *
     * @param payerCostSelected user selected payerCost.
     */
    override fun onPayerCostSelected(payerCostSelected: PayerCost) {
        val customOptionId = customOptionIdSolver[getCurrentOneTapItem()]
        val selected = amountConfigurationRepository.getConfigurationSelectedFor(customOptionId)!!
            .getAppliedPayerCost(state.splitSelectionState.userWantsToSplit())
            .indexOf(payerCostSelected)
        updateElementPosition(selected)
        getVariant(KnownVariant.SCROLLED).process(object : VariantHandler {
            override fun visit(variant: ScrolledVariant) {
                if (variant.isDefault()) {
                    view.collapseInstallmentsSelection()
                }
            }
        })
    }

    fun onDisabledDescriptorViewClick() {
        val payerPaymentMethodId = customOptionIdSolver[getCurrentOneTapItem()]
        val application = applicationSelectionRepository[payerPaymentMethodId]

        val paymentTypeId = application.paymentMethod.type
        view.showDisabledPaymentMethodDetailDialog(
            disabledPaymentMethodRepository[PayerPaymentMethodKey(payerPaymentMethodId, paymentTypeId)]!!,
            application.status
        )
    }

    override fun onDiscountAmountDescriptorClicked(discountModel: DiscountConfigurationModel) {
        view.showDiscountDetailDialog(paymentSettingRepository.currency, discountModel)
    }

    override fun onChargesAmountDescriptorClicked(dynamicDialogCreator: DynamicDialogCreator) {
        val checkoutData = DynamicDialogCreator.CheckoutData(
            paymentSettingRepository.checkoutPreference!!, listOf(PaymentData())
        )
        view.showDynamicDialog(dynamicDialogCreator, checkoutData)
    }

    override fun onSplitChanged(isChecked: Boolean) {
        if (state.splitSelectionState.userWantsToSplit() != isChecked) {
            resetPayerCostSelection()
        }
        state.splitSelectionState.setUserWantsToSplit(isChecked)
        // cancel also update the position.
        // it is used because the installment selection can be expanded by the user.
        onInstallmentSelectionCanceled()
    }

    override fun onHeaderClicked() {
        val checkoutPreference = paymentSettingRepository.checkoutPreference
        val dynamicDialogConfiguration = paymentSettingRepository.advancedConfiguration.dynamicDialogConfiguration
        val checkoutData = DynamicDialogCreator.CheckoutData(checkoutPreference!!, listOf(PaymentData()))
        if (dynamicDialogConfiguration.hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER)) {
            view.showDynamicDialog(
                dynamicDialogConfiguration.getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER)!!,
                checkoutData
            )
        }
    }

    private fun resetPayerCostSelection() {
        payerCostSelectionRepository.reset()
    }

    override fun onPostPaymentAction(postPaymentAction: PostPaymentAction) {
        postPaymentAction.execute(object : ActionController {
            override fun recoverPayment(postPaymentAction: PostPaymentAction) {
                //nothing to do here
            }

            override fun onChangePaymentMethod() {
                postDisableModelUpdate()
            }
        })
    }

    private fun postDisableModelUpdate() {
        oneTapItemRepository.sortByState()
        if (isViewAttached) {
            reload()
        }
    }

    override fun onOtherPaymentMethodClicked() {
        view.showOfflineMethodsExpanded()
    }

    @SuppressLint("WrongConstant")
    override fun handlePrePaymentAction(callback: OnReadyForPaymentCallback) {
        if (!handleBehaviour(CheckoutBehaviour.Type.TAP_PAY)) {
            requireCurrentConfiguration(callback)
        }
    }

    private fun handleBehaviour(@CheckoutBehaviour.Type behaviourType: String): Boolean {
        val oneTapItem = getCurrentOneTapItem()
        val behaviour = oneTapItem.getBehaviour(behaviourType)
        val modal = if (behaviour?.modal != null) modalRepository.value[behaviour.modal!!] else null
        val target = behaviour?.target
        val isMethodSuspended = oneTapItem.status.isSuspended
        return when {
            modal != null -> {
                view.showGenericDialog(
                    FromModalToGenericDialogItem(
                        ActionTypeWrapper(oneTapItemRepository.value).actionType, behaviour!!.modal!!
                    ).map(modal)
                )
                true
            }
            TextUtil.isNotEmpty(target) -> {
                track(TargetBehaviourEvent(viewTrack!!, TargetBehaviourTrackData(behaviourType, target!!)))
                view.startDeepLink(target)
                true
            }
            isMethodSuspended -> {
                // is a friction if the method is suspended and does not have any behaviour to handle
                track(SuspendedFrictionTracker)
                true
            }
            else -> false
        }
    }

    private fun requireCurrentConfiguration(callback: OnReadyForPaymentCallback) {
        val oneTapItem = getCurrentOneTapItem()
        val configuration = FromExpressMetadataToPaymentConfiguration(
            amountConfigurationRepository,
            state.splitSelectionState,
            payerCostSelectionRepository,
            applicationSelectionRepository,
            customOptionIdSolver
        ).map(oneTapItem)
        callback.call(configuration)
    }

    override fun handleGenericDialogAction(type: ActionType) {
        val actionTypeWrapper = ActionTypeWrapper(oneTapItemRepository.value)
        when (type) {
            ActionType.PAY_WITH_OTHER_METHOD, ActionType.PAY_WITH_OFFLINE_METHOD -> view.setPagerIndex(actionTypeWrapper.indexToReturn)
            ActionType.ADD_NEW_CARD -> {
                view.setPagerIndex(actionTypeWrapper.indexToReturn)
                view.startAddNewCardFlow(
                    CardFormWrapper(
                        paymentSettingRepository, trackingRepository, authorizationProvider
                    )
                )
            }
            else -> Unit
        }
    }

    override fun onPaymentExecuted(configuration: PaymentConfiguration) {
        val experiments: MutableList<Experiment> = ArrayList()
        val confirmData = ConfirmData(
            ConfirmData.ReviewType.ONE_TAP, state.paymentMethodIndex,
            FromSelectedExpressMetadataToAvailableMethods(
                applicationSelectionRepository, fromApplicationToApplicationInfo,
                escManagerBehaviour.escCardIds, configuration.payerCost, configuration.splitPayment
            ).map(getCurrentOneTapItem())
        )
        val experiment = experimentsRepository.getExperiment(KnownExperiment.INSTALLMENTS_HIGHLIGHT)
        if (getCurrentPayerCosts().size > 1 && experiment != null) {
            experiments.add(experiment)
        }
        track(ConfirmEvent(confirmData, experiments))
    }

    private fun overridePayButtonStateChange(uiState: PayButton.State): Boolean {
        val payerPaymentMethodId = customOptionIdSolver[getCurrentOneTapItem()]
        val paymentTypeId = applicationSelectionRepository[payerPaymentMethodId].paymentMethod.type
        return uiState === PayButton.State.ENABLE && (getCurrentOneTapItem().isNewCard ||
                getCurrentOneTapItem().isOfflineMethods || disabledPaymentMethodRepository.hasKey(
            PayerPaymentMethodKey(payerPaymentMethodId, paymentTypeId)
        ))
    }

    private fun reload() {
        resetPayerCostSelection()
        resetState()
        view.clearAdapters()
        loadViewModel()
    }

    override fun handleDeepLink() {
        disabledPaymentMethodRepository.reset()
        if (isViewAttached) {
            view.showLoading()
        }
        checkoutUseCase.execute(Unit, {
            if (isViewAttached) {
                reload()
                view.hideLoading()
            }
        }, {
            if (isViewAttached) {
                view.hideLoading()
                onFailToRetrieveInitResponse(it.apiException)
            }
        })
    }

    override fun onCardAdded(cardId: String, callback: LifecycleListener.Callback) {
        checkoutWithNewCardUseCase.execute(cardId, { callback.onSuccess() }, { callback.onError() })
    }

    override fun onCardFormResult() {
        postDisableModelUpdate()
    }

    override fun onApplicationChanged(paymentTypeId: String) {
        val oneTapItem = getCurrentOneTapItem()
        for (application in oneTapItem.getApplications()) {
            if (application.paymentMethod.type == paymentTypeId) {
                applicationSelectionRepository[oneTapItem] = application
                updateElements()
            }
        }
    }

    private fun getVariants() = ExperimentHelper.getVariantsFrom(
        experimentsRepository.experiments, KnownVariant.PULSE, KnownVariant.BADGE, KnownVariant.SCROLLED
    )

    private fun getVariant(knownVariant: KnownVariant) = getVariantFrom(experimentsRepository.experiments, knownVariant)
}
